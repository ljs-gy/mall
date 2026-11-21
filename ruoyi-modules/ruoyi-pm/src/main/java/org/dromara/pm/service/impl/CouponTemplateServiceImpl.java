package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.domain.PromoScopeTarget;
import org.dromara.pm.domain.PromoScopeUser;
import org.dromara.pm.domain.bo.CouponTemplateAuditBo;
import org.dromara.pm.domain.bo.CouponTemplateBo;
import org.dromara.pm.domain.vo.CouponTemplateVo;
import org.dromara.pm.mapper.CouponTemplateMapper;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.mapper.PromoScopeTargetMapper;
import org.dromara.pm.mapper.PromoScopeUserMapper;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.mq.CouponDirectAllPageMessage;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.ICouponTemplateService;
import org.dromara.pm.utils.CouponValidTimeUtils;
import org.dromara.pm.utils.PromoScopeUtils;
import org.dromara.user.api.RemoteUserBizService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 优惠券Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CouponTemplateServiceImpl implements ICouponTemplateService {

    private static final long SOURCE_TYPE_COUPON = 2L;
    private static final String GRANT_GIFT = "GIFT";
    private static final String GRANT_DIRECT = "DIRECT";
    private static final int DIRECT_ALL_PAGE_SIZE = 500;

    private final CouponTemplateMapper baseMapper;
    private final PromoScopeTargetMapper promoScopeTargetMapper;
    private final PromoScopeUserMapper promoScopeUserMapper;
    private final CouponUserRelMapper couponUserRelMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationContext applicationContext;

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    @Override
    public CouponTemplateVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<CouponTemplateVo> queryPageList(CouponTemplateBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CouponTemplate> lqw = buildQueryWrapper(bo);
        Page<CouponTemplateVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<CouponTemplateVo> queryList(CouponTemplateBo bo) {
        LambdaQueryWrapper<CouponTemplate> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CouponTemplate> buildQueryWrapper(CouponTemplateBo bo) {
        LambdaQueryWrapper<CouponTemplate> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(CouponTemplate::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), CouponTemplate::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getCouponType()), CouponTemplate::getCouponType, bo.getCouponType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrantType()), CouponTemplate::getGrantType, bo.getGrantType());
        lqw.eq(bo.getScopeItemType() != null, CouponTemplate::getScopeItemType, bo.getScopeItemType());
        lqw.eq(bo.getStatus() != null, CouponTemplate::getStatus, bo.getStatus());
        return lqw;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(CouponTemplateBo bo) {
        if (bo.getPlatformShareRate() != null || bo.getMerchantShareRate() != null) {
            if (bo.getPlatformShareRate() == null || bo.getMerchantShareRate() == null) {
                throw new ServiceException("平台分摊比例与商家分摊比例需同时填写");
            }
            if (bo.getPlatformShareRate().add(bo.getMerchantShareRate()).compareTo(BigDecimal.ONE) != 0) {
                throw new ServiceException("平台分摊比例与商家分摊比例之和必须等于 100%");
            }
        }
        if ("REDUCE".equals(bo.getCouponType())) {
            if (bo.getBenefitValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("满减券的减免面额必须大于 0");
            }
            if (bo.getMinSpendAmount().compareTo(bo.getBenefitValue()) < 0) {
                throw new ServiceException("最低消费门槛不能小于减免面额");
            }
        } else if ("DISCOUNT".equals(bo.getCouponType())) {
            if (bo.getBenefitValue().compareTo(BigDecimal.ZERO) <= 0
                || bo.getBenefitValue().compareTo(BigDecimal.ONE) >= 0) {
                throw new ServiceException("折扣率必须在 0~1 之间（不含边界）");
            }
        } else {
            throw new ServiceException("不支持的优惠券类型：" + bo.getCouponType());
        }
        if (bo.getScopeItemType() == null) {
            throw new ServiceException("商品范围类型不能为空");
        }
        CouponValidTimeUtils.validateExclusive(bo.getUseStartTime(), bo.getUseEndTime(), bo.getValidMinutes());
        CouponValidTimeUtils.validateRelativeClaimWindow(
            bo.getClaimStartTime(), bo.getClaimEndTime(),
            bo.getUseStartTime(), bo.getUseEndTime(), bo.getValidMinutes());
        if (bo.getClaimStartTime() != null && bo.getClaimEndTime() != null && bo.getClaimEndTime().before(bo.getClaimStartTime())) {
            throw new ServiceException("领用结束时间不能早于开始时间");
        }
        if (bo.getUseStartTime() != null && bo.getUseEndTime() != null && bo.getUseEndTime().before(bo.getUseStartTime())) {
            throw new ServiceException("使用结束时间不能早于开始时间");
        }
        if (CouponValidTimeUtils.isFixed(bo.getUseStartTime(), bo.getUseEndTime(), bo.getValidMinutes())
            && bo.getClaimStartTime() != null && bo.getClaimEndTime() != null) {
            if (bo.getClaimStartTime().before(bo.getUseStartTime())
                || bo.getClaimEndTime().after(bo.getUseEndTime())) {
                throw new ServiceException("固定有效期下，领取时间须落在券可用时段内");
            }
        }
        validateDirectGrantCreate(bo);
        CouponTemplate add = MapstructUtils.convert(bo, CouponTemplate.class);
        add.setTenantId(LoginHelper.getTenantId());
        add.setChannelCode(bo.getChannelCode());
        add.setStatus(0L);
        if (baseMapper.insert(add) <= 0) {
            throw new ServiceException("优惠券模板创建失败");
        }
        bo.setId(add.getId());
        Long templateId = add.getId();
        List<PromoScopeTarget> targetList = new ArrayList<>();
        if (Long.valueOf(1L).equals(bo.getScopeSupplierType()) && CollUtil.isNotEmpty(bo.getSupplierCodes())) {
            for (String code : bo.getSupplierCodes()) {
                if (StringUtils.isBlank(code)) {
                    continue;
                }
                PromoScopeTarget target = new PromoScopeTarget();
                target.setId(IdGeneratorUtil.nextLongId());
                target.setTenantId(add.getTenantId());
                target.setSourceType(SOURCE_TYPE_COUPON);
                target.setSourceId(templateId);
                target.setTargetCategory(PmCacheKeys.CAT_SUPPLIER);
                target.setTargetId(code.trim());
                targetList.add(target);
            }
        }
        Long itemCategory = PromoScopeUtils.resolveItemTargetCategory(bo.getScopeItemType());
        if (itemCategory != null && CollUtil.isNotEmpty(bo.getItemIds())) {
            for (Long itemId : bo.getItemIds()) {
                PromoScopeTarget target = new PromoScopeTarget();
                target.setId(IdGeneratorUtil.nextLongId());
                target.setTenantId(add.getTenantId());
                target.setSourceType(SOURCE_TYPE_COUPON);
                target.setSourceId(templateId);
                target.setTargetCategory(itemCategory);
                target.setTargetId(String.valueOf(itemId));
                targetList.add(target);
            }
        }
        if (CollUtil.isNotEmpty(targetList)) {
            promoScopeTargetMapper.batchInsert(targetList);
        }

        if (!GRANT_GIFT.equals(bo.getGrantType()) && PromoScopeUtils.isSpecifiedUserScope(bo.getUserScopeType()) && CollUtil.isNotEmpty(bo.getUserTargetIds())) {
            List<PromoScopeUser> userList = new ArrayList<>();
            for (Long targetId : bo.getUserTargetIds()) {
                PromoScopeUser userScope = new PromoScopeUser();
                userScope.setId(IdGeneratorUtil.nextLongId());
                userScope.setTenantId(add.getTenantId());
                userScope.setSourceType(SOURCE_TYPE_COUPON);
                userScope.setSourceId(templateId);
                userScope.setTargetId(targetId);
                userList.add(userScope);
            }
            promoScopeUserMapper.batchInsert(userList);
        }
        log.info("优惠券模板创建成功，已提交审核，模板ID: {}, grantType: {}", templateId, bo.getGrantType());
        return true;
    }

    @Override
    public Boolean updateByBo(CouponTemplateBo bo) {
        CouponTemplate update = MapstructUtils.convert(bo, CouponTemplate.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 审核优惠券模板
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCouponTemplate(CouponTemplateAuditBo bo) {
        CouponTemplate template = baseMapper.selectById(bo.getTemplateId());
        if (template == null) {
            throw new ServiceException("优惠券不存在");
        }
        if (!Long.valueOf(0L).equals(template.getStatus())) {
            throw new ServiceException("优惠券非待审核状态");
        }
        if (Boolean.FALSE.equals(bo.getPassFlag())) {
            template.setStatus(5L);
            template.setAuditBy(LoginHelper.getUserId());
            template.setAuditTime(new Date());
            template.setAuditRemark(bo.getAuditRemark());
            baseMapper.updateById(template);
            log.info("优惠券模板审核被驳回，模板ID: {}", template.getId());
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime statusStart = toLocalDateTime(template.getClaimStartTime());
        LocalDateTime statusEnd = toLocalDateTime(template.getClaimEndTime());
        if (statusStart == null && statusEnd == null) {
            statusStart = toLocalDateTime(template.getUseStartTime());
            statusEnd = toLocalDateTime(template.getUseEndTime());
        }
        long initStatus;
        // 当前时间还没到开始时间，未开始
        if (statusStart != null && now.isBefore(statusStart)) {
            initStatus = 1L;
        } else if (statusEnd != null && !now.isBefore(statusEnd)) {
            initStatus = 3L;
        } else {
            initStatus = 2L;
        }
        template.setStatus(initStatus);
        template.setAuditBy(LoginHelper.getUserId());
        template.setAuditTime(new Date());
        template.setAuditRemark(bo.getAuditRemark());
        baseMapper.updateById(template);
        if (Long.valueOf(3L).equals(initStatus)) {
            return;
        }
        boolean directDispatch = GRANT_DIRECT.equals(template.getGrantType()) && Long.valueOf(2L).equals(initStatus);
        Long templateId = template.getId();
        LocalDateTime delayStart = statusStart;
        LocalDateTime delayEnd = statusEnd;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheCouponTemplate(templateId);
                scheduleCouponStatusDelayMq(template, now, delayStart, delayEnd);
                if (directDispatch) {
                    dispatchDirectSend(template);
                }
            }
        });
    }

    private void validateDirectGrantCreate(CouponTemplateBo bo) {
        if (!GRANT_DIRECT.equals(bo.getGrantType())) {
            return;
        }
        if (bo.getDirectPerUserCount() == null || bo.getDirectPerUserCount() <= 0) {
            throw new ServiceException("定向券每人发放张数必须大于0");
        }
        if (bo.getSendType() == null) {
            throw new ServiceException("定向券发送时间类型不能为空");
        }
        if (Long.valueOf(2L).equals(bo.getSendType())) {
            validateDirectTimedSendTime(bo);
        }
        if (Long.valueOf(1L).equals(bo.getUserScopeType())) {
            if (CollUtil.isEmpty(bo.getUserTargetIds())) {
                throw new ServiceException("指定用户定向券必须配置用户名单");
            }
        } else if (Long.valueOf(2L).equals(bo.getUserScopeType())) {
            if (CollUtil.isEmpty(bo.getUserTargetIds())) {
                throw new ServiceException("指定会员等级定向券必须配置等级");
            }
        } else if (Long.valueOf(0L).equals(bo.getUserScopeType())) {
            if (CollUtil.isNotEmpty(bo.getUserTargetIds())) {
                throw new ServiceException("全部用户定向券无需配置用户名单");
            }
        } else {
            throw new ServiceException("定向券仅支持全部用户、指定用户或指定会员等级");
        }
    }

    /**
     * 定时发送：sendTime 必填、不得早于当前，且须落在模板有效时间窗（优先领用，否则使用）内。
     */
    private void validateDirectTimedSendTime(CouponTemplateBo bo) {
        Date sendTime = bo.getSendTime();
        if (sendTime == null) {
            throw new ServiceException("定时发送必须填写发送时间");
        }
        Date now = new Date();
        if (sendTime.before(now)) {
            throw new ServiceException("定时发送时间不能早于当前时间");
        }
        Date windowStart = bo.getClaimStartTime();
        Date windowEnd = bo.getClaimEndTime();
        if (windowStart == null && windowEnd == null) {
            windowStart = bo.getUseStartTime();
            windowEnd = bo.getUseEndTime();
        }
        if (windowStart == null && windowEnd == null) {
            throw new ServiceException("定时发送需配置领用或使用时间窗口");
        }
        if (windowStart != null && sendTime.before(windowStart)) {
            throw new ServiceException("定时发送时间不能早于优惠券有效开始时间");
        }
        if (windowEnd != null && sendTime.after(windowEnd)) {
            throw new ServiceException("定时发送时间不能晚于优惠券有效结束时间");
        }
    }


    @Override
    public void dispatchDirectSend(CouponTemplate template) {
        if (!GRANT_DIRECT.equals(template.getGrantType())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (Long.valueOf(1L).equals(template.getSendType())) {
            startDirectGrant(template);
            return;
        }
        if (Long.valueOf(2L).equals(template.getSendType())) {
            scheduleDirectTimedSend(template, now);
        }
    }


    /**
     * 给定时发送的定向券挂到点再发的延迟 MQ
     */
    private void scheduleDirectTimedSend(CouponTemplate template, LocalDateTime now) {
        long ttl = Duration.between(now, toLocalDateTime(template.getSendTime())).toMillis();
        if (ttl <= 0) {
            startDirectGrant(template);
            return;
        }
        rabbitTemplate.convertAndSend(
            CouponMqConstants.EXCHANGE_PM_DELAY,
            CouponMqConstants.KEY_COUPON_DIRECT_SEND_DELAY,
            template.getId(),
            message -> {
                message.getMessageProperties().setDelayLong(ttl);
                return message;
            }
        );
    }

    /**
     * 定向券真正开始发放的入口 （审核已进行中，或延时开始触发后）
     */
    @Override
    public void startDirectGrant(CouponTemplate template) {
        ICouponTemplateService self = applicationContext.getBean(ICouponTemplateService.class);
        // 全员，投 MQ分页接力发放
        if (Long.valueOf(0L).equals(template.getUserScopeType())) {
            publishDirectScanPage(template, 0L);
            log.info("定向券模板 {} 已投递全员直发首页消息", template.getId());
            return;
        }
        if (Long.valueOf(2L).equals(template.getUserScopeType())) {
            List<Long> levelIds = promoScopeUserMapper.selectUserIdsBySourceId(template.getId());
            if (CollUtil.isEmpty(levelIds)) {
                log.warn("定向券模板 {} 未配置会员等级，跳过发放", template.getId());
                return;
            }
            List<Long> userIds = remoteUserBizService.listBuyerUserinfoIdsByDistLevels(template.getTenantId(), template.getChannelCode(), levelIds);
            self.sendDirectCouponsAsync(template, template.getDirectPerUserCount(), userIds);
            log.info("定向券模板 {} 按等级直发已提交异步，levels={} users={}", template.getId(), levelIds, userIds == null ? 0 : userIds.size());
            return;
        }
        if (Long.valueOf(1L).equals(template.getUserScopeType())) {
            List<Long> userIds = promoScopeUserMapper.selectUserIdsBySourceId(template.getId());
            self.sendDirectCouponsAsync(template, template.getDirectPerUserCount(), userIds);
        }
    }

    private void publishDirectScanPage(CouponTemplate template, long lastUserId) {
        CouponDirectAllPageMessage msg = new CouponDirectAllPageMessage();
        msg.setTemplateId(template.getId());
        msg.setTenantId(template.getTenantId());
        msg.setChannelCode(template.getChannelCode());
        msg.setLastUserId(lastUserId);
        msg.setPageSize(DIRECT_ALL_PAGE_SIZE);
        rabbitTemplate.convertAndSend(
            CouponMqConstants.EXCHANGE_PM_DELAY,
            CouponMqConstants.KEY_COUPON_DIRECT_ALL_PAGE,
            msg,
            message -> {
                message.getMessageProperties().setDelayLong(0L);
                return message;
            });
    }



    private void scheduleCouponStatusDelayMq(CouponTemplate template, LocalDateTime now, LocalDateTime statusStart, LocalDateTime statusEnd) {
        Long templateId = template.getId();
        if (Long.valueOf(1L).equals(template.getStatus())) {
            long ttlToStart = Duration.between(now, statusStart).toMillis();
            if (ttlToStart > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_COUPON_START_DELAY,
                    templateId,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlToStart);
                        return message;
                    }
                );
                log.info("优惠券模板 {} 将在 {} 毫秒后自动变更为 [进行中]", templateId, ttlToStart);
            }
        }
        if ((Long.valueOf(1L).equals(template.getStatus()) || Long.valueOf(2L).equals(template.getStatus()))
            && statusEnd != null) {
            long ttlToEnd = Duration.between(now, statusEnd).toMillis();
            if (ttlToEnd > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_COUPON_END_DELAY,
                    templateId,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlToEnd);
                        return message;
                    }
                );
                log.info("优惠券模板 {} 将在 {} 毫秒后自动变更为 [已结束]", templateId, ttlToEnd);
            }
        }
    }

    /**
     * 审核通过把这张券预热进 Redis
     */
    @Override
    public void cacheCouponTemplate(Long templateId) {
        CouponTemplate template = baseMapper.selectById(templateId);
        if (template == null) {
            return;
        }
        Date now = new Date();
        String tenant = template.getTenantId();
        String channel = template.getChannelCode();
        Date claimEndTime = template.getClaimEndTime();
        Date useEndTime = template.getUseEndTime();
        Date ttlEnd = claimEndTime != null ? claimEndTime : useEndTime;
        long ttlSeconds = calculateTtlSeconds(now, ttlEnd);
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.couponDetail(tenant, channel, templateId),
            JsonUtils.toJsonString(template),
            ttlSeconds,
            TimeUnit.SECONDS);
        String grantType = template.getGrantType();
        List<PromoScopeTarget> targets = promoScopeTargetMapper.selectBySourceId(templateId, SOURCE_TYPE_COUPON);
        if (GRANT_GIFT.equals(grantType)) {
            cacheCouponTargetScopes(template, targets, ttlSeconds, false);
            log.info("满赠券模板 {} 预热完成（detail + 核销 target）", templateId);
            return;
        }
        if (GRANT_DIRECT.equals(grantType)) {
            cacheCouponTargetScopes(template, targets, ttlSeconds, false);
            cacheCouponUserScopes(template, templateId, ttlSeconds);
            log.info("定向券模板 {} 预热完成（detail + 核销 target + 用户范围）", templateId);
            return;
        }
        if (template.getTotalCount() != null && template.getTotalCount() > 0) {
            stringRedisTemplate.opsForValue().setIfAbsent(
                PmCacheKeys.couponStock(tenant, channel, templateId),
                String.valueOf(template.getTotalCount()),
                ttlSeconds,
                TimeUnit.SECONDS);
        }
        cacheCouponTargetScopes(template, targets, ttlSeconds, true);
        cacheCouponUserScopes(template, templateId, ttlSeconds);
        if (Long.valueOf(2L).equals(template.getStatus())) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.couponActiveIds(tenant, channel), templateId.toString());
        }
        log.info("普通券模板 {} 缓存预热完成", templateId);
    }


    @Override
    public void clearCouponCache(Long templateId) {
        CouponTemplate template = baseMapper.selectById(templateId);
        List<PromoScopeTarget> targets =
            promoScopeTargetMapper.selectBySourceId(templateId, SOURCE_TYPE_COUPON);
        String tenant = template.getTenantId();
        String channel = template.getChannelCode();
        stringRedisTemplate.delete(List.of(
            PmCacheKeys.couponDetail(tenant, channel, templateId),
            PmCacheKeys.couponStock(tenant, channel, templateId),
            PmCacheKeys.couponUserLimit(tenant, channel, templateId),
            PmCacheKeys.couponUser(tenant, channel, "white", templateId),
            PmCacheKeys.couponUser(tenant, channel, "black", templateId),
            PmCacheKeys.couponLevel(tenant, channel, "white", templateId),
            PmCacheKeys.couponLevel(tenant, channel, "black", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "supplier", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "site", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "merchant", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "category", "white", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "category", "black", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "brand", "white", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "brand", "black", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "sku", "white", templateId),
            PmCacheKeys.couponTarget(tenant, channel, "sku", "black", templateId)
        ));
        String idStr = String.valueOf(templateId);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.couponActiveIds(tenant, channel), idStr);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.couponScopeAll(tenant, channel), idStr);
        for (PromoScopeTarget target : targets) {
            if (!PmCacheKeys.isItemCategory(target.getTargetCategory())) {
                continue;
            }
            stringRedisTemplate.opsForSet().remove(
                PmCacheKeys.couponScope(tenant, channel, target.getTargetCategory(), target.getTargetId()),
                idStr);
        }
        log.info("优惠券模板 {} 缓存及索引清理完成", templateId);
    }


    /**
     * 把优惠劵的商品/供应商适用范围存储到Redis
     */
    private void cacheCouponTargetScopes(CouponTemplate template, List<PromoScopeTarget> targets, long ttlSeconds, boolean writeCendScope) {
        Long templateId = template.getId();
        String idStr = String.valueOf(templateId);
        String tenant = template.getTenantId();
        String channel = template.getChannelCode();
        boolean allItems = Long.valueOf(0L).equals(template.getScopeItemType());
        Long itemMode = template.getItemParticipateType();
        boolean itemBlack = !allItems && Long.valueOf(2L).equals(itemMode);
        // 全场或指定黑名单写全场活动池
        if (writeCendScope && (allItems || itemBlack)) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.couponScopeAll(tenant, channel), idStr);
        }
        if (CollUtil.isEmpty(targets)) {
            return;
        }
        String itemModeTag = PmCacheKeys.modeTag(itemMode);
        for (PromoScopeTarget target : targets) {
            Long cat = target.getTargetCategory();
            String dim = PmCacheKeys.targetDim(cat);
            if (Long.valueOf(PmCacheKeys.CAT_SUPPLIER).equals(cat)) {
                if (!Long.valueOf(1L).equals(template.getScopeSupplierType())) {
                    continue;
                }
                String key = PmCacheKeys.couponTarget(tenant, channel, dim, templateId);
                stringRedisTemplate.opsForSet().add(key, target.getTargetId());
                stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
                continue;
            }
            if (!allItems && PmCacheKeys.isItemCategory(cat)) {
                String forwardKey = PmCacheKeys.couponTarget(tenant, channel, dim, itemModeTag, templateId);
                stringRedisTemplate.opsForSet().add(forwardKey, target.getTargetId());
                stringRedisTemplate.expire(forwardKey, ttlSeconds, TimeUnit.SECONDS);
                // 仅白名单写
                if (writeCendScope && !itemBlack) {
                    stringRedisTemplate.opsForSet().add(PmCacheKeys.couponScope(tenant, channel, cat, target.getTargetId()), idStr);
                }
            }
        }
    }

    private void cacheCouponUserScopes(CouponTemplate template, Long templateId, long ttlSeconds) {
        if (!PromoScopeUtils.isSpecifiedUserScope(template.getUserScopeType())) {
            return;
        }
        List<PromoScopeUser> userScopeList =
            promoScopeUserMapper.selectListBySourceId(templateId, SOURCE_TYPE_COUPON);
        if (CollUtil.isEmpty(userScopeList)) {
            return;
        }
        String tenant = template.getTenantId();
        String channel = template.getChannelCode();
        String modeTag = PmCacheKeys.modeTag(template.getUserParticipateType());
        boolean isLevel = Long.valueOf(2L).equals(template.getUserScopeType());
        String key = isLevel
            ? PmCacheKeys.couponLevel(tenant, channel, modeTag, templateId)
            : PmCacheKeys.couponUser(tenant, channel, modeTag, templateId);
        for (PromoScopeUser scopeUser : userScopeList) {
            stringRedisTemplate.opsForSet().add(key, String.valueOf(scopeUser.getTargetId()));
        }
        stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    private long calculateTtlSeconds(Date now, Date targetEndTime) {
        if (targetEndTime == null) {
            return 604800L;
        }
        long remainSeconds = (targetEndTime.getTime() - now.getTime()) / 1000;
        if (remainSeconds <= 0) {
            return 3600L;
        }
        return remainSeconds + 7200L;
    }


    @Override
    @Async
    public void sendDirectCouponsAsync(CouponTemplate template, Long perUserCount, List<Long> userIds) {
        grantDirectCoupons(template, perUserCount, userIds);
    }


    /**
     * 负责把定向券真正写进用户券包
     */
    private void grantDirectCoupons(CouponTemplate template, Long perUserCount, List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        List<Long> granted = couponUserRelMapper.selectExistingUserIds(template.getId(), userIds);
        List<Long> toGrant = userIds.stream().filter(id -> !granted.contains(id)).collect(Collectors.toList());
        if (CollUtil.isEmpty(toGrant)) {
            log.info("定向券模板 {} 本批用户均已发放，跳过", template.getId());
            return;
        }
        Date[] window = CouponValidTimeUtils.resolveUserWindow(template, new Date());
        List<CouponUserRel> relList = new ArrayList<>();
        for (Long userId : toGrant) {
            for (int i = 0; i < perUserCount; i++) {
                CouponUserRel rel = new CouponUserRel();
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
                rel.setId(IdGeneratorUtil.nextLongId());
                relList.add(rel);
            }
        }
        List<List<CouponUserRel>> partitions = CollUtil.split(relList, 500);
        for (List<CouponUserRel> part : partitions) {
            couponUserRelMapper.batchInsert(part);
        }
        log.info("定向券发放完成，模板ID: {}, 本批用户: {}, 跳过已发: {}, 写入张数: {}", template.getId(), toGrant.size(), granted.size(), relList.size());
    }

    @Override
    public void processDirectAllPage(CouponDirectAllPageMessage message) {
        CouponTemplate template = baseMapper.selectById(message.getTemplateId());
        if (template == null) {
            return;
        }
        int pageSize = message.getPageSize();
        List<Long> pageIds = remoteUserBizService.pageBuyerUserinfoIds(message.getTenantId(), message.getChannelCode(), message.getLastUserId(), pageSize);
        if (CollUtil.isEmpty(pageIds)) {
            log.info("定向券模板 {} 全员直发完成（无更多买家），lastUserId={}", template.getId(), message.getLastUserId());
            return;
        }
        grantDirectCoupons(template, template.getDirectPerUserCount(), pageIds);
        long nextLast = pageIds.get(pageIds.size() - 1);
        if (pageIds.size() >= pageSize) {
            publishDirectScanPage(template, nextLast);
            log.info("定向券模板 {} 全员直发已处理至 userId={}，继续下一页", template.getId(), nextLast);
        } else {
            log.info("定向券模板 {} 全员直发完成，末页 lastUserId={}", template.getId(), nextLast);
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
