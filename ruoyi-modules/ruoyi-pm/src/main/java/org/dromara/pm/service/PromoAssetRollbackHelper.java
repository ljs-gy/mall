package org.dromara.pm.service;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.AfterSaleRefundedEvent;
import org.dromara.order.api.mq.OrderCancelledEvent;
import org.dromara.order.api.mq.SkuRollbackItem;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.mapper.PromoGoodsSkuMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 取消 / 售后共用的营销资产回滚（MySQL + Redis），调用方决定走预占解锁还是已售回退。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromoAssetRollbackHelper {

    private static final String TYPE_SECKILL = "SECKILL";

    private final PromoGoodsSkuMapper promoGoodsSkuMapper;
    private final PromoExclusiveAssetService promoExclusiveAssetService;
    private final PromoUserQuotaService promoUserQuotaService;
    private final CouponUserRelMapper couponUserRelMapper;


    public void rollbackOnCancel(OrderCancelledEvent event) {
        if (CollUtil.isNotEmpty(event.getExclusiveItems())) {
            unlockExclusive(event.getTenantId(), event.getChannelCode(), event.getOrderNo(), event.getUserId(), event.getActivityId(), event.getExclusivePromoType(), event.getExclusiveItems());
        }
        if (!Boolean.TRUE.equals(event.getRollbackSharedAssets())) {
            return;
        }
        rollbackShared(event.getTenantId(), event.getChannelCode(), event.getParentOrderNo(), event.getUserId(),
            event.getUserCouponId(), event.getCrossActivityId(), event.getCrossDiscountAmount(),
            event.getPromoJoinActivityIds());
    }

    public void rollbackOnAfterSale(AfterSaleRefundedEvent event) {
        if (CollUtil.isNotEmpty(event.getExclusiveItems())) {
            rollbackSoldExclusive(event.getTenantId(), event.getChannelCode(), event.getAfterSaleNo(), event.getUserId(), event.getActivityId(), event.getExclusivePromoType(), event.getExclusiveItems());
        }
        if (!Boolean.TRUE.equals(event.getRollbackSharedAssets())) {
            return;
        }
        rollbackShared(event.getTenantId(), event.getChannelCode(), event.getParentOrderNo(), event.getUserId(),
            event.getUserCouponId(), event.getCrossActivityId(), event.getCrossDiscountAmount(),
            event.getPromoJoinActivityIds());
    }

    /**
     * 还专享预占用（仅 Redis；DB 无 locked_stock，未支付取消不改 sold）
     */
    private void unlockExclusive(String tenantId, String channel, String orderNo, Long userId, Long activityId, String exclusivePromoType, List<SkuRollbackItem> items) {
        restoreExclusiveRedis(tenantId, channel, orderNo, userId, activityId, exclusivePromoType, items);
    }

    private void rollbackSoldExclusive(String tenantId, String channel, String afterSaleNo, Long userId,
                                       Long activityId, String exclusivePromoType, List<SkuRollbackItem> items) {
        for (SkuRollbackItem item : items) {
            promoGoodsSkuMapper.rollbackSoldPromoStock(tenantId, activityId, item.getSkuId(), item.getBuyCount());
        }
        restoreExclusiveRedis(tenantId, channel, afterSaleNo, userId, activityId, exclusivePromoType, items);
    }

    private void rollbackShared(String tenantId, String channel, String parentOrderNo, Long userId,
                               Long userCouponId, Long crossActivityId, BigDecimal crossDiscountAmount,
                               List<Long> promoJoinActivityIds) {
        couponUserRelMapper.rollbackCoupon(tenantId, userCouponId, userId, new Date());
        rollbackCrossBudgetRedis(tenantId, channel, parentOrderNo, crossActivityId, crossDiscountAmount);
        rollbackJoinQuotaRedis(tenantId, channel, parentOrderNo, userId, promoJoinActivityIds);
    }

    private void restoreExclusiveRedis(String tenantId, String channel, String idempotentNo, Long userId,
                                       Long activityId, String exclusivePromoType, List<SkuRollbackItem> items) {
        if (!promoExclusiveAssetService.tryMarkAssetRefunded(tenantId, channel, idempotentNo, PmCacheKeys.REFUND_ASSET_EXCLUSIVE)) {
            log.info("专享 Redis 已退过，跳过 idempotentNo={}", idempotentNo);
            return;
        }
        try {
            promoExclusiveAssetService.restoreExclusiveByItems(
                tenantId, channel, activityId, TYPE_SECKILL.equalsIgnoreCase(exclusivePromoType),
                userId, items);
            log.info("专享 Redis 已退还 idempotentNo={}", idempotentNo);
        } catch (RuntimeException e) {
            promoExclusiveAssetService.clearAssetRefundedMark(tenantId, channel, idempotentNo, PmCacheKeys.REFUND_ASSET_EXCLUSIVE);
            throw e;
        }
    }

    /**
     * 把父单占用的跨店满减/满折预算加回 Redis，只在父单全取消或售后整单退完时
     */
    private void rollbackCrossBudgetRedis(String tenantId, String channel, String parentOrderNo, Long crossActivityId, BigDecimal crossDiscountAmount) {
        if (!promoExclusiveAssetService.tryMarkAssetRefunded(tenantId, channel, parentOrderNo, PmCacheKeys.REFUND_ASSET_BUDGET)) {
            log.info("跨店预算已退过，跳过 parentOrderNo={}", parentOrderNo);
            return;
        }
        try {
            promoExclusiveAssetService.restoreCrossPromoBudget(tenantId, channel, crossActivityId, crossDiscountAmount);
            log.info("跨店预算已退还 parentOrderNo={} actId={}", parentOrderNo, crossActivityId);
        } catch (RuntimeException e) {
            promoExclusiveAssetService.clearAssetRefundedMark(tenantId, channel, parentOrderNo, PmCacheKeys.REFUND_ASSET_BUDGET);
            throw e;
        }
    }

    /**
     * 把活动参与次数退回 Redis。
     */
    private void rollbackJoinQuotaRedis(String tenantId, String channel, String parentOrderNo, Long userId, List<Long> promoJoinActivityIds) {
        if (!promoExclusiveAssetService.tryMarkAssetRefunded(tenantId, channel, parentOrderNo, PmCacheKeys.REFUND_ASSET_JOIN_LIMIT)) {
            log.info("参与次数已退过，跳过 parentOrderNo={}", parentOrderNo);
            return;
        }
        try {
            promoUserQuotaService.rollbackJoinQuota(tenantId, channel, userId, promoJoinActivityIds);
            log.info("参与次数已退还 parentOrderNo={}", parentOrderNo);
        } catch (RuntimeException e) {
            promoExclusiveAssetService.clearAssetRefundedMark(tenantId, channel, parentOrderNo, PmCacheKeys.REFUND_ASSET_JOIN_LIMIT);
            throw e;
        }
    }

}
