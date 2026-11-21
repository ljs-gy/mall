package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.bo.ExclusivePromoRef;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderPaidAssetConfirmService;
import org.dromara.order.service.IOrderPromoWriteService;
import org.dromara.pm.api.RemotePmService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 子单支付成功：仅结转本单物理库存 + 专享配额（券已在提交时核销）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaidAssetConfirmServiceImpl implements IOrderPaidAssetConfirmService {

    private static final long PAY_STATUS_PAID = 1L;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final IOrderPromoWriteService orderPromoWriteService;
    private final StringRedisTemplate stringRedisTemplate;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    @DubboReference
    private RemotePmService remotePmService;

    @Override
    public void confirmAssets(OrderPaidEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId()) || StringUtils.isBlank(event.getOrderNo())) {
            log.warn("ORDER_PAID 资产结转参数不完整，跳过 event={}", event);
            return;
        }
        TenantHelper.dynamic(event.getTenantId(), () -> doConfirm(event));
    }

    private void doConfirm(OrderPaidEvent event) {
        String tenantId = event.getTenantId();
        String orderNo = event.getOrderNo();
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
        if (order == null) {
            log.warn("ORDER_PAID 资产结转子单不存在 orderNo={}", orderNo);
            return;
        }
        // 必须已支付
        if (PAY_STATUS_PAID != order.getPayStatus()) {
            throw new IllegalStateException("ORDER_PAID 资产结转时子单尚未已付 orderNo=" + orderNo);
        }
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        confirmPhysicalStock(items);
        confirmExclusivePromoStock(items, tenantId);
        log.info("ORDER_PAID 资产结转完成 orderNo={}", orderNo);
    }

    /**
     * 支付成功后，确认物理库存的锁定：把下单时占住的 lock_stock 结转掉（真正卖出）。
     */
    private void confirmPhysicalStock(List<OrderItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (OrderItem item : items) {
            if (item.getSkuId() == null) {
                continue;
            }
            int qty = item.getQuantity() == null ? 0 : item.getQuantity().intValue();
            if (qty <= 0) {
                continue;
            }
            boolean ok = remoteGoodsService.confirmPhysicalStock(item.getSkuId(), qty);
            if (ok) {
                log.info("物理锁定库存已确认 lock_stock-= skuId={} qty={}", item.getSkuId(), qty);
            } else {
                log.info("物理锁定库存确认幂等跳过 skuId={} qty={}", item.getSkuId(), qty);
            }
        }
    }


    private void confirmExclusivePromoStock(List<OrderItem> items, String tenantId) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        // 收集要查专享的行 ID
        List<Long> candidateIds = new ArrayList<>();
        for (OrderItem item : items) {
            // 排查赠品
            if (item.getIsGift() != null && item.getIsGift() == 1L) {
                continue;
            }
            if (item.getId() != null) {
                candidateIds.add(item.getId());
            }
        }
        // 哪些行挂了专享活动
        Map<Long, ExclusivePromoRef> exclusiveMap = orderPromoWriteService.loadExclusivePromos(tenantId, candidateIds);
        if (exclusiveMap.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            ExclusivePromoRef ref = exclusiveMap.get(item.getId());
            // 非专享行跳过
            if (ref == null) {
                continue;
            }
            Long activityId = ref.getActivityId();
            Long skuId = item.getSkuId();
            int qty = item.getQuantity().intValue();
            // 无 locked 后靠占坑防 MQ 重投重复 +sold
            Boolean claimed = stringRedisTemplate.opsForValue().setIfAbsent(
                OrderCacheKeys.promoSoldClaimed(tenantId, item.getId()),
                "1",
                OrderCacheKeys.PROMO_SOLD_CLAIM_TTL_SECONDS,
                TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(claimed)) {
                log.info("活动配额确认幂等跳过 orderItemId={} activityId={} skuId={}", item.getId(), activityId, skuId);
                continue;
            }
            try {
                int rows = remotePmService.confirmPromoStock(tenantId, activityId, skuId, qty);
                if (rows > 0) {
                    log.info("活动配额已确认 sold+= activityId={} skuId={} qty={}", activityId, skuId, qty);
                } else {
                    stringRedisTemplate.delete(OrderCacheKeys.promoSoldClaimed(tenantId, item.getId()));
                    log.warn("活动配额确认未更新行 activityId={} skuId={} qty={}，已释放占坑", activityId, skuId, qty);
                }
            } catch (RuntimeException e) {
                stringRedisTemplate.delete(OrderCacheKeys.promoSoldClaimed(tenantId, item.getId()));
                throw e;
            }
        }
    }
}
