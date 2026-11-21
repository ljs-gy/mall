package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.CouponGiftGrantRule;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.mapper.CouponTemplateMapper;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.service.IOrderPaidGiftGrantService;
import org.dromara.pm.utils.CouponValidTimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 支付成功满额送券：按子单实付匹配规则 → Redis 占坑幂等 → 落券包。
 */
@Slf4j
@Service
public class OrderPaidGiftGrantServiceImpl implements IOrderPaidGiftGrantService {

    private static final String GRANT_GIFT = "GIFT";

    private final StringRedisTemplate stringRedisTemplate;
    private final CouponTemplateMapper couponTemplateMapper;
    private final CouponUserRelMapper couponUserRelMapper;
    private final OrderPaidGiftGrantServiceImpl self;

    public OrderPaidGiftGrantServiceImpl(StringRedisTemplate stringRedisTemplate,
                                        CouponTemplateMapper couponTemplateMapper,
                                        CouponUserRelMapper couponUserRelMapper,
                                        @Lazy OrderPaidGiftGrantServiceImpl self) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.couponTemplateMapper = couponTemplateMapper;
        this.couponUserRelMapper = couponUserRelMapper;
        this.self = self;
    }

    /**
     * 用本子单实付匹配进行中的满赠规则，符合条件就发券。
     */
    @Override
    public void grantOnOrderPaid(OrderPaidEvent event) {
        if (StringUtils.isBlank(event.getOrderNo()) || event.getUserId() == null) {
            log.warn("ORDER_PAID 满额送券参数不完整，跳过 event={}", event);
            return;
        }
        BigDecimal paidAmount = event.getPaidAmount();
        if (paidAmount == null) {
            log.warn("ORDER_PAID 满额送券缺少子单实付金额，跳过 orderNo={}", event.getOrderNo());
            return;
        }
        String tenant = event.getTenantId();
        String channel = event.getChannelCode();
        Date now = event.getPayTime();
        // 拉取进行中的满赠规则
        Set<String> grantIds = stringRedisTemplate.opsForSet().members(PmCacheKeys.couponGrantActiveIds(tenant, channel));
        if (CollUtil.isEmpty(grantIds)) {
            log.info("ORDER_PAID 无进行中满赠规则 orderNo={}", event.getOrderNo());
            return;
        }
        List<String> idList = new ArrayList<>(grantIds);
        List<String> detailKeys = idList.stream()
            .map(id -> PmCacheKeys.couponGrantDetail(tenant, channel, Long.valueOf(id)))
            .toList();
        List<String> detailJsons = stringRedisTemplate.opsForValue().multiGet(detailKeys);
        for (String json : detailJsons) {
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponGiftGrantRule rule = JsonUtils.parseObject(json, CouponGiftGrantRule.class);
            // 支付时刻不在活动起止时间内跳过
            if (!inTimeWindow(rule.getStartTime(), rule.getEndTime(), now)) {
                continue;
            }
            // 与商详一致：规则物权必须等于子单物权
            if (!StringUtils.equals(rule.getMemberCode(), event.getMemberCode())) {
                continue;
            }
            // 本子单实付不够跳过
            BigDecimal minOrderAmount = rule.getMinOrderAmount();
            if (minOrderAmount == null || paidAmount.compareTo(minOrderAmount) < 0) {
                continue;
            }
            // 不符合用户范围不发
            if (!checkUserEligibility(rule.getUserScopeType(), rule.getUserParticipateType(),
                event.getUserId(), event.getUserLevelId(), rule.getId(), tenant, channel)) {
                continue;
            }
            self.tryGrantOneRule(event, channel, rule, rule.getGiveCouponCount().intValue());
        }
        log.info("ORDER_PAID 满额送券完成 orderNo={} paidAmount={}", event.getOrderNo(), paidAmount);
    }

    /**
     * 按子单幂等发满赠券（同渠道同时仅一条有效规则）
     */
    @Transactional(rollbackFor = Exception.class)
    public void tryGrantOneRule(OrderPaidEvent event, String channel, CouponGiftGrantRule rule, int giveCount) {
        String claimKey = PmCacheKeys.couponGrantClaimed(event.getTenantId(), channel, event.getOrderNo());
        // 防同一子单因 MQ 重投重复发券
        Boolean claimed = stringRedisTemplate.opsForValue().setIfAbsent(claimKey, "1", PmCacheKeys.GIFT_GRANT_CLAIM_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(claimed)) {
            log.info("满额送券已处理过，跳过 orderNo={}", event.getOrderNo());
            return;
        }
        try {
            CouponTemplate template = loadTemplate(rule.getCouponTemplateId(), event.getTenantId(), channel);
            if (template == null) {
                log.warn("满额送券模板不存在 templateId={} ruleId={}", rule.getCouponTemplateId(), rule.getId());
                stringRedisTemplate.delete(claimKey);
                return;
            }
            if (!GRANT_GIFT.equalsIgnoreCase(template.getGrantType())) {
                log.warn("满额送券模板发放方式非 GIFT，跳过 templateId={} grantType={}", template.getId(), template.getGrantType());
                stringRedisTemplate.delete(claimKey);
                return;
            }
            List<CouponUserRel> relList = buildCouponRels(template, event.getUserId(), giveCount);
            int inserted = couponUserRelMapper.batchInsert(relList);
            if (inserted <= 0) {
                throw new ServiceException("满额送券落库失败 orderNo=" + event.getOrderNo() + " ruleId=" + rule.getId());
            }
            log.info("满额送券成功 orderNo={} ruleId={} templateId={} count={}", event.getOrderNo(), rule.getId(), template.getId(), giveCount);
        } catch (RuntimeException e) {
            stringRedisTemplate.delete(claimKey);
            throw e;
        }
    }

    /**
     * 按模板给用户造 giveCount 张待入库的券实例
     */
    private List<CouponUserRel> buildCouponRels(CouponTemplate template, Long userId, int giveCount) {
        Date[] window = CouponValidTimeUtils.resolveUserWindow(template, new Date());
        List<CouponUserRel> list = new ArrayList<>(giveCount);
        for (int i = 0; i < giveCount; i++) {
            CouponUserRel rel = new CouponUserRel();
            rel.setId(IdGeneratorUtil.nextLongId());
            rel.setTenantId(template.getTenantId());
            rel.setUserId(userId);
            rel.setCouponTemplateId(template.getId());
            rel.setChannelCode(template.getChannelCode());
            rel.setTitle(template.getTitle());
            rel.setCouponType(template.getCouponType());
            rel.setImgUrl(template.getImgUrl());
            rel.setUserCouponCode(IdUtil.getSnowflakeNextIdStr());
            rel.setMinSpendAmount(template.getMinSpendAmount());
            rel.setBenefitValue(template.getBenefitValue());
            rel.setStartTime(window[0]);
            rel.setEndTime(window[1]);
            rel.setStatus(0L);
            list.add(rel);
        }
        return list;
    }

    private CouponTemplate loadTemplate(Long templateId, String tenant, String channel) {
        String detailJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.couponDetail(tenant, channel, templateId));
        if (StringUtils.isNotBlank(detailJson)) {
            CouponTemplate cached = JsonUtils.parseObject(detailJson, CouponTemplate.class);
            if (cached != null) {
                return cached;
            }
        }
        return couponTemplateMapper.selectById(templateId);
    }

    private boolean checkUserEligibility(Long userScopeType, Long userParticipateType, Long userId, Long userLevelId,
                                         Long ruleId, String tenant, String channel) {
        // 不限人群直接放行
        if (userScopeType == null || Long.valueOf(0L).equals(userScopeType)) {
            return true;
        }
        int participate = userParticipateType.intValue();
        boolean white = participate != 2;
        if (Long.valueOf(1L).equals(userScopeType)) {
            String key = PmCacheKeys.couponGrantUser(tenant, channel, white ? "white" : "black", ruleId);
            boolean hit = Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userId)));
            return white == hit;
        }
        if (Long.valueOf(2L).equals(userScopeType)) {
            if (userLevelId == null) {
                return false;
            }
            String key = PmCacheKeys.couponGrantLevel(tenant, channel, white ? "white" : "black", ruleId);
            boolean hit = Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userLevelId)));
            return white == hit;
        }
        return true;
    }

    /**
     * 判断当前时间是否落在活动有效期内
     */
    /** 半开区间 [start, end)：结束瞬间不算有效 */
    private boolean inTimeWindow(Date start, Date end, Date now) {
        if (start == null || end == null) {
            return false;
        }
        return !start.after(now) && end.after(now);
    }
}
