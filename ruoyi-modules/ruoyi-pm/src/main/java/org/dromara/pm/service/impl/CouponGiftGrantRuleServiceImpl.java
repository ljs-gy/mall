package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.pm.domain.CouponGiftGrantRule;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.PromoScopeUser;
import org.dromara.pm.domain.bo.CouponGiftGrantRuleAuditBo;
import org.dromara.pm.domain.bo.CouponGiftGrantRuleBo;
import org.dromara.pm.domain.vo.CouponGiftGrantRuleVo;
import org.dromara.pm.mapper.CouponGiftGrantRuleMapper;
import org.dromara.pm.mapper.CouponTemplateMapper;
import org.dromara.pm.mapper.PromoScopeUserMapper;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.ICouponGiftGrantRuleService;
import org.dromara.pm.utils.PromoScopeUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 满赠券发放规则Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CouponGiftGrantRuleServiceImpl implements ICouponGiftGrantRuleService {

    private static final long SOURCE_TYPE_GIFT_RULE = 3L;
    private static final String GRANT_GIFT = "GIFT";
    private static final List<Long> ACTIVE_OR_PENDING_STATUSES = List.of(0L, 1L, 2L);

    private final CouponGiftGrantRuleMapper baseMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final PromoScopeUserMapper promoScopeUserMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public CouponGiftGrantRuleVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<CouponGiftGrantRuleVo> queryPageList(CouponGiftGrantRuleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CouponGiftGrantRule> lqw = buildQueryWrapper(bo);
        Page<CouponGiftGrantRuleVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<CouponGiftGrantRuleVo> queryList(CouponGiftGrantRuleBo bo) {
        LambdaQueryWrapper<CouponGiftGrantRule> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CouponGiftGrantRule> buildQueryWrapper(CouponGiftGrantRuleBo bo) {
        LambdaQueryWrapper<CouponGiftGrantRule> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(CouponGiftGrantRule::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), CouponGiftGrantRule::getTitle, bo.getTitle());
        lqw.eq(bo.getCouponTemplateId() != null, CouponGiftGrantRule::getCouponTemplateId, bo.getCouponTemplateId());
        lqw.eq(bo.getUserScopeType() != null, CouponGiftGrantRule::getUserScopeType, bo.getUserScopeType());
        lqw.eq(bo.getStatus() != null, CouponGiftGrantRule::getStatus, bo.getStatus());
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(CouponGiftGrantRuleBo bo) {
        CouponTemplate template = couponTemplateMapper.selectById(bo.getCouponTemplateId());
        validateGiftRuleAgainstTemplate(bo, template, true);
        assertNoOverlappingGiftGrant(bo.getChannelCode(), bo.getStartTime(), bo.getEndTime(), null);
        CouponGiftGrantRule add = MapstructUtils.convert(bo, CouponGiftGrantRule.class);
        add.setTenantId(LoginHelper.getTenantId());
        add.setChannelCode(bo.getChannelCode());
        add.setStatus(0L);
        if (baseMapper.insert(add) <= 0) {
            throw new ServiceException("满赠券发放规则创建失败");
        }
        bo.setId(add.getId());
        if (PromoScopeUtils.isSpecifiedUserScope(bo.getUserScopeType()) && CollUtil.isNotEmpty(bo.getUserTargetIds())) {
            saveScopeUsers(add.getId(), add.getTenantId(), bo);
        }
        log.info("满赠规则 {} 创建成功，提交待审核", add.getId());
        return true;
    }


    private void validateGiftRuleAgainstTemplate(CouponGiftGrantRuleBo bo, CouponTemplate template, boolean checkUserTargets) {
        if (template == null || !GRANT_GIFT.equals(template.getGrantType())) {
            throw new ServiceException("选中的优惠券模板无效或非满赠券类型！");
        }
        if (StringUtils.isNotBlank(bo.getChannelCode())
            && StringUtils.isNotBlank(template.getChannelCode())
            && !bo.getChannelCode().equals(template.getChannelCode())) {
            throw new ServiceException("满赠发放规则渠道必须与优惠券模板渠道一致！");
        }
        Long status = template.getStatus();
        if (!Long.valueOf(1L).equals(status) && !Long.valueOf(2L).equals(status)) {
            throw new ServiceException("所选优惠券须为待生效或进行中状态！");
        }
        if (bo.getEndTime() != null && bo.getStartTime() != null && bo.getEndTime().before(bo.getStartTime())) {
            throw new ServiceException("活动结束时间不能早于开始时间");
        }
        if (checkUserTargets && PromoScopeUtils.isSpecifiedUserScope(bo.getUserScopeType()) && CollUtil.isEmpty(bo.getUserTargetIds())) {
            throw new ServiceException("指定用户时请选择用户或会员等级");
        }
        Date couponStart = template.getUseStartTime();
        Date couponEnd = template.getUseEndTime();
        if (bo.getStartTime() != null && couponStart != null && bo.getStartTime().before(couponStart)) {
            throw new ServiceException("满赠活动开始时间不能早于被赠送优惠券的生效开始时间");
        }
        if (bo.getEndTime() != null && couponEnd != null && bo.getEndTime().after(couponEnd)) {
            throw new ServiceException("满赠活动结束时间不能晚于被赠送优惠券的生效结束时间");
        }
    }


    /**
     * 同渠道满赠送券规则的时间互斥：同一渠道下，不允许两段还有效的规则在时间上交叉。
     */
    private void assertNoOverlappingGiftGrant(String channelCode, Date startTime, Date endTime, Long excludeId) {
        Long count = baseMapper.selectCount(Wrappers.<CouponGiftGrantRule>lambdaQuery()
            .eq(CouponGiftGrantRule::getChannelCode, channelCode)
            .in(CouponGiftGrantRule::getStatus, ACTIVE_OR_PENDING_STATUSES)
            .ne(excludeId != null, CouponGiftGrantRule::getId, excludeId)
            .le(CouponGiftGrantRule::getStartTime, endTime)
            .ge(CouponGiftGrantRule::getEndTime, startTime));
        if (count > 0) {
            throw new ServiceException("当前时间区间内已存在有效的满赠活动，不可重复创建");
        }
    }

    private void saveScopeUsers(Long ruleId, String tenantId, CouponGiftGrantRuleBo bo) {
        List<PromoScopeUser> userList = new ArrayList<>();
        for (Long targetId : bo.getUserTargetIds()) {
            PromoScopeUser userScope = new PromoScopeUser();
            userScope.setId(IdGeneratorUtil.nextLongId());
            userScope.setTenantId(tenantId);
            userScope.setSourceType(SOURCE_TYPE_GIFT_RULE);
            userScope.setSourceId(ruleId);
            userScope.setTargetId(targetId);
            userList.add(userScope);
        }
        promoScopeUserMapper.batchInsert(userList);
    }

    @Override
    public Boolean updateByBo(CouponGiftGrantRuleBo bo) {
        CouponTemplate template = couponTemplateMapper.selectById(bo.getCouponTemplateId());
        validateGiftRuleAgainstTemplate(bo, template, true);
        assertNoOverlappingGiftGrant(bo.getChannelCode(), bo.getStartTime(), bo.getEndTime(), bo.getId());
        CouponGiftGrantRule update = MapstructUtils.convert(bo, CouponGiftGrantRule.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditGiftGrantRule(CouponGiftGrantRuleAuditBo bo) {
        CouponGiftGrantRule rule = baseMapper.selectById(bo.getId());
        if (rule == null) {
            throw new ServiceException("满赠规则不存在");
        }
        if (!Long.valueOf(0L).equals(rule.getStatus())) {
            throw new ServiceException("满赠规则非待审核状态");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = toLocalDateTime(rule.getStartTime());
        LocalDateTime endTime = toLocalDateTime(rule.getEndTime());
        rule.setAuditBy(LoginHelper.getUserId());
        rule.setAuditTime(new Date());
        rule.setAuditRemark(bo.getAuditRemark());
        if (!Boolean.TRUE.equals(bo.getPassFlag())) {
            rule.setStatus(5L);
            baseMapper.updateById(rule);
            log.info("满赠规则 {} 审核被驳回", rule.getId());
            return;
        }
        long initStatus;
        if (startTime != null && now.isBefore(startTime)) {
            initStatus = 1L;
        } else if (endTime == null || now.isBefore(endTime)) {
            initStatus = 2L;
        } else {
            initStatus = 3L;
        }
        rule.setStatus(initStatus);
        baseMapper.updateById(rule);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (Long.valueOf(1L).equals(rule.getStatus()) || Long.valueOf(2L).equals(rule.getStatus())) {
                    cacheGiftGrantRule(rule.getId());
                }
                scheduleGiftGrantRuleDelayMq(rule, now, startTime, endTime);
            }
        });

        log.info("满赠规则 {} 审核通过，当前状态: {}", rule.getId(), rule.getStatus());
    }

    private void scheduleGiftGrantRuleDelayMq(CouponGiftGrantRule rule, LocalDateTime now,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        Long ruleId = rule.getId();
        if (Long.valueOf(1L).equals(rule.getStatus())) {
            long ttlToStart = Duration.between(now, startTime).toMillis();
            if (ttlToStart > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_GIFT_RULE_START_DELAY,
                    ruleId,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlToStart);
                        return message;
                    }
                );
                log.info("满赠规则 {} 将在 {} 毫秒后自动变更为 [进行中]", ruleId, ttlToStart);
            }
        }
        if (Long.valueOf(1L).equals(rule.getStatus()) || Long.valueOf(2L).equals(rule.getStatus())) {
            long ttlToEnd = Duration.between(now, endTime).toMillis();
            if (ttlToEnd > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_GIFT_RULE_END_DELAY,
                    ruleId,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlToEnd);
                        return message;
                    }
                );
                log.info("满赠规则 {} 将在 {} 毫秒后自动变更为 [已结束]", ruleId, ttlToEnd);
            }
        }
    }

    /**
     * 预热满赠发放规则缓存
     */
    @Override
    public void cacheGiftGrantRule(Long ruleId) {
        CouponGiftGrantRule rule = baseMapper.selectById(ruleId);
        if (rule == null) {
            return;
        }
        long ttlSeconds = (rule.getEndTime().getTime() - System.currentTimeMillis()) / 1000 + 86400L;
        String idStr = String.valueOf(ruleId);
        String tenant = rule.getTenantId();
        String channel = rule.getChannelCode();
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.couponGrantDetail(tenant, channel, ruleId),
            JsonUtils.toJsonString(rule),
            ttlSeconds,
            TimeUnit.SECONDS);
        if (PromoScopeUtils.isSpecifiedUserScope(rule.getUserScopeType())) {
            List<PromoScopeUser> scopeUsers = promoScopeUserMapper.selectListBySourceId(ruleId, SOURCE_TYPE_GIFT_RULE);
            if (CollUtil.isNotEmpty(scopeUsers)) {
                String modeTag = PmCacheKeys.modeTag(rule.getUserParticipateType());
                boolean isLevel = Long.valueOf(2L).equals(rule.getUserScopeType());
                String scopeKey = isLevel
                    ? PmCacheKeys.couponGrantLevel(tenant, channel, modeTag, ruleId)
                    : PmCacheKeys.couponGrantUser(tenant, channel, modeTag, ruleId);
                for (PromoScopeUser scopeUser : scopeUsers) {
                    stringRedisTemplate.opsForSet().add(scopeKey, String.valueOf(scopeUser.getTargetId()));
                }
                stringRedisTemplate.expire(scopeKey, ttlSeconds, TimeUnit.SECONDS);
            }
        }
        // detail/人群/display_ids：预热+进行中；active_ids：仅进行中（勿写入 couponScope*，那是券模板发现池）
        stringRedisTemplate.opsForSet().add(PmCacheKeys.couponGrantDisplayIds(tenant, channel), idStr);
        if (Long.valueOf(2L).equals(rule.getStatus())) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.couponGrantActiveIds(tenant, channel), idStr);
        }
        log.info("满赠规则 {} 缓存预热完成 status={}", ruleId, rule.getStatus());
    }

    @Override
    public void clearGiftGrantRuleCache(Long ruleId) {
        String idStr = String.valueOf(ruleId);
        CouponGiftGrantRule rule = baseMapper.selectById(ruleId);
        String tenant = rule != null ? rule.getTenantId() : null;
        String channel = rule != null ? rule.getChannelCode() : null;
        stringRedisTemplate.delete(List.of(
            PmCacheKeys.couponGrantDetail(tenant, channel, ruleId),
            PmCacheKeys.couponGrantUser(tenant, channel, "white", ruleId),
            PmCacheKeys.couponGrantUser(tenant, channel, "black", ruleId),
            PmCacheKeys.couponGrantLevel(tenant, channel, "white", ruleId),
            PmCacheKeys.couponGrantLevel(tenant, channel, "black", ruleId)
        ));
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.couponGrantDisplayIds(tenant, channel), idStr);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.couponGrantActiveIds(tenant, channel), idStr);
        log.info("满赠规则 {} 缓存已清理", ruleId);
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
