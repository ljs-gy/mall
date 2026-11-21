package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.api.domain.RemoteAfterSaleRefundConfirmBo;
import org.dromara.order.api.mq.AfterSaleRefundTriggerMessage;
import org.dromara.order.api.mq.AfterSaleRefundedEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.SkuRollbackItem;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.bo.ExclusivePromoRef;
import org.dromara.order.domain.bo.SharedPromoAssetsBo;
import org.dromara.order.mapper.AfterSaleItemMapper;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IAfterSaleRefundService;
import org.dromara.order.service.IOrderPromoWriteService;
import org.dromara.order.util.OrderItemRefundOccupyWriter;
import org.dromara.pay.api.RemotePayService;
import org.dromara.pay.api.domain.RemoteWxRefundBo;
import org.dromara.pay.api.domain.RemoteWxRefundResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AfterSaleRefundServiceImpl implements IAfterSaleRefundService {

    private static final Long STATUS_REFUNDING = 40L;
    private static final Long STATUS_REFUNDED = 50L;

    private static final Long ORDER_STATUS_PENDING_SHIP = 20L;
    private static final Long ORDER_STATUS_CANCELLED = 50L;

    private static final Long PAY_STATUS_FULL_REFUND = 2L;
    private static final Long PAY_STATUS_PARTIAL_REFUND = 3L;

    private static final Long IS_GIFT = 1L;

    private final RabbitTemplate rabbitTemplate;
    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleItemMapper afterSaleItemMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRefundOccupyWriter orderItemRefundOccupyWriter;
    private final IOrderPromoWriteService orderPromoWriteService;

    @DubboReference
    private RemotePayService remotePayService;

    @Lazy
    @Autowired
    private IAfterSaleRefundService self;


    @Override
    public void triggerRefundAfterCommit(String tenantId, String afterSaleNo) {
        AfterSaleRefundTriggerMessage message = AfterSaleRefundTriggerMessage.builder()
            .tenantId(tenantId)
            .afterSaleNo(afterSaleNo)
            .build();
        afterCommit(() -> {
            try {
                rabbitTemplate.convertAndSend(
                    OrderMqConstants.EXCHANGE_AFTER_SALE_REFUND_TRIGGER,
                    OrderMqConstants.KEY_AFTER_SALE_REFUND_TRIGGER,
                    message);
                log.info("已投递售后退款触发消息 afterSaleNo={}", afterSaleNo);
            } catch (Exception e) {
                log.error("投递售后退款触发消息失败 afterSaleNo={}", afterSaleNo, e);
            }
        });
    }


    @Override
    public void executeRefundTrigger(String tenantId, String afterSaleNo) {
        TenantHelper.dynamic(tenantId, () -> doExecuteRefundTrigger(tenantId, afterSaleNo));
    }

    private void doExecuteRefundTrigger(String tenantId, String afterSaleNo) {
        AfterSale afterSale = loadAfterSale(tenantId, afterSaleNo);
        if (afterSale == null) {
            throw new ServiceException("售后单不存在");
        }
        Long status = afterSale.getStatus();
        if (STATUS_REFUNDED.equals(status)) {
            log.info("售后已退款完成，跳过触发 afterSaleNo={}", afterSaleNo);
            return;
        }
        if (!STATUS_REFUNDING.equals(status)) {
            log.warn("售后非退款中，跳过触发 afterSaleNo={} status={}", afterSaleNo, status);
            return;
        }
        // 先查微信
        RemoteWxRefundResult queried = tryQueryRefund(afterSaleNo);
        if (queried != null) {
            if (RemoteWxRefundResult.SUCCESS.equals(queried.getStatus())) {
                RemoteAfterSaleRefundConfirmBo confirmBo = new RemoteAfterSaleRefundConfirmBo();
                confirmBo.setTenantId(tenantId);
                confirmBo.setAfterSaleNo(afterSaleNo);
                confirmBo.setRefundTradeNo(queried.getRefundId());
                confirmBo.setRefundTime(new Date());
                self.confirmRefunded(confirmBo);
                return;
            }
            if (RemoteWxRefundResult.PROCESSING.equals(queried.getStatus())) {
                log.info("微信退款处理中，等待回调 afterSaleNo={}", afterSaleNo);
                return;
            }
            throw new ServiceException("微信退款状态异常: " + queried.getStatus());
        }
        createWxRefund(afterSale);
    }


    private RemoteWxRefundResult tryQueryRefund(String afterSaleNo) {
        try {
            return remotePayService.queryRefund(afterSaleNo);
        } catch (Exception e) {
            log.info("微信退款查询未命中或失败，将尝试发起 afterSaleNo={} err={}", afterSaleNo, e.getMessage());
            return null;
        }
    }

    private void createWxRefund(AfterSale afterSale) {
        String afterSaleNo = afterSale.getAfterSaleNo();
        String tenantId = afterSale.getTenantId();
        BigDecimal refundYuan = afterSale.getRefundAmount();
        int refundCent = toCent(refundYuan);
        if (refundCent <= 0) {
            throw new ServiceException("退款金额必须大于0");
        }
        Order order = loadOrder(tenantId, afterSale.getOrderNo());
        if (order == null) {
            throw new ServiceException("关联订单不存在");
        }
        String payTradeNo = order.getPayTradeNo();
        if (StringUtils.isBlank(payTradeNo)) {
            throw new ServiceException("订单支付流水号为空，无法退款");
        }
        int totalCent = resolveOriginalPayCent(tenantId, order);
        RemoteWxRefundBo refundBo = new RemoteWxRefundBo();
        refundBo.setTransactionId(payTradeNo);
        refundBo.setOutRefundNo(afterSaleNo);
        refundBo.setRefundAmountCent((long) refundCent);
        refundBo.setTotalAmountCent((long) totalCent);
        refundBo.setReason(afterSale.getApplyReason());
        RemoteWxRefundResult result = remotePayService.createRefund(refundBo);
        if (result == null) {
            throw new ServiceException("发起微信退款失败");
        }
        if (RemoteWxRefundResult.SUCCESS.equals(result.getStatus())) {
            RemoteAfterSaleRefundConfirmBo confirmBo = new RemoteAfterSaleRefundConfirmBo();
            confirmBo.setTenantId(tenantId);
            confirmBo.setAfterSaleNo(afterSaleNo);
            confirmBo.setRefundTradeNo(result.getRefundId());
            confirmBo.setRefundTime(new Date());
            self.confirmRefunded(confirmBo);
            return;
        }
        log.info("微信退款已受理，等待回调 afterSaleNo={} status={}", afterSaleNo, result.getStatus());
    }

    /**
     * 微信已经退款成功之后，把售后更新为退款完成，写本地促销逆向明细，发送MQ广播回滚库存、营销
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmRefunded(RemoteAfterSaleRefundConfirmBo bo) {
        AfterSale afterSale = loadAfterSale(bo.getTenantId(), bo.getAfterSaleNo());
        String tenantId = afterSale.getTenantId();
        Long status = afterSale.getStatus();
        // 退款完成，直接返回
        if (STATUS_REFUNDED.equals(status)) {
            return true;
        }
        if (!STATUS_REFUNDING.equals(status)) {
            log.warn("售后非退款中，拒绝确认 afterSaleNo={} status={}", bo.getAfterSaleNo(), status);
            return false;
        }
        Date refundTime = bo.getRefundTime();
        // 更新为退款完成
        int rows = afterSaleMapper.refundSuccessByStatusCas(tenantId, bo.getAfterSaleNo(), STATUS_REFUNDING, STATUS_REFUNDED, refundTime, bo.getRefundTradeNo());
        if (rows <= 0) {
            return false;
        }
        List<AfterSaleItem> items = new ArrayList<>(loadAfterSaleItems(tenantId, bo.getAfterSaleNo()));
        // 本售后主品：申请中 → 已退成功
        orderItemRefundOccupyWriter.moveRefundingToRefunded(items);
        // 主品退完时系统收回赠品（直接记已退成功）
        List<AfterSaleItem> giftItems = reclaimGiftsIfMainFullyRefunded(tenantId, afterSale);
        if (CollUtil.isNotEmpty(giftItems)) {
            orderItemRefundOccupyWriter.addRefunded(giftItems);
            items.addAll(giftItems);
            log.info("主品已退完，系统收回赠品 afterSaleNo={} giftLines={}", bo.getAfterSaleNo(), giftItems.size());
        }
        // 回写订单状态
        refreshOrderRefundFlags(tenantId, afterSale.getOrderNo());
        Map<Long, Integer> refundQtyMap = new HashMap<>();
        for (AfterSaleItem item : items) {
            refundQtyMap.put(item.getOrderItemId(), item.getReturnQuantity());
        }
        Order order = loadOrder(tenantId, afterSale.getOrderNo());
        String parentOrderNo = order.getParentOrderNo();
        // 写入促销逆向明细
        orderPromoWriteService.writeReverseOnRefund(tenantId, parentOrderNo, bo.getAfterSaleNo(), refundQtyMap);
        // 判断这个父单下所有子单、所有明细 是否都已退完
        boolean parentClosed = isParentFullyRefunded(tenantId, parentOrderNo);
        if (parentClosed) {
            orderPromoWriteService.casReleaseSharedAssets(tenantId, parentOrderNo);
        }
        publishRefundedEventsAfterCommit(afterSale, items, order, parentClosed);
        log.info("售后退款完成 afterSaleNo={} refundTradeNo={} parentClosed={}", bo.getAfterSaleNo(), bo.getRefundTradeNo(), parentClosed);
        return true;
    }


    private List<AfterSaleItem> reclaimGiftsIfMainFullyRefunded(String tenantId, AfterSale afterSale) {
        String orderNo = afterSale.getOrderNo();
        String afterSaleNo = afterSale.getAfterSaleNo();
        List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        if (CollUtil.isEmpty(orderItems)) {
            return List.of();
        }
        // 看主品是否都退完（读订单行已退数量）
        for (OrderItem oi : orderItems) {
            if (IS_GIFT.equals(oi.getIsGift())) {
                continue;
            }
            int buyQty = oi.getQuantity().intValue();
            int refundQty = nullToZero(oi.getRefundedQty());
            if (refundQty < buyQty) {
                return List.of();
            }
        }
        List<AfterSaleItem> giftItems = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            if (!IS_GIFT.equals(oi.getIsGift())) {
                continue;
            }
            int buyQty = oi.getQuantity().intValue();
            int refundQty = nullToZero(oi.getRefundedQty());
            int remain = buyQty - refundQty;
            if (remain == 0) {
                continue;
            }
            AfterSaleItem asi = new AfterSaleItem();
            asi.setTenantId(tenantId);
            asi.setAfterSaleNo(afterSaleNo);
            asi.setOrderNo(orderNo);
            asi.setOrderItemId(oi.getId());
            asi.setSkuId(oi.getSkuId());
            asi.setSkuCode(oi.getSkuCode());
            asi.setGoodsTitle(oi.getGoodsTitle());
            asi.setSpecText(oi.getSpecText());
            asi.setCoverImage(oi.getCoverImage());
            asi.setReturnQuantity(remain);
            asi.setRefundPrice(BigDecimal.ZERO);
            asi.setRefundAmount(BigDecimal.ZERO);
            afterSaleItemMapper.insert(asi);
            giftItems.add(asi);
        }
        return giftItems;
    }

    private List<AfterSaleItem> loadAfterSaleItems(String tenantId, String afterSaleNo) {
        return afterSaleItemMapper.selectList(Wrappers.<AfterSaleItem>lambdaQuery()
            .eq(AfterSaleItem::getTenantId, tenantId)
            .eq(AfterSaleItem::getAfterSaleNo, afterSaleNo));
    }


    private void publishRefundedEventsAfterCommit(AfterSale afterSale, List<AfterSaleItem> afterItems, Order order, boolean parentClosed) {
        AfterSaleRefundedEvent event = buildAfterSaleRefundedEvent(afterSale, afterItems, order, parentClosed);
        if (event == null) {
            return;
        }
        String afterSaleNo = afterSale.getAfterSaleNo();
        afterCommit(() -> {
            try {
                rabbitTemplate.convertAndSend(OrderMqConstants.EXCHANGE_AFTER_SALE_REFUNDED, "", event);
                log.info("已发送 AFTER_SALE_REFUNDED afterSaleNo={} parentClosed={} physicalLines={}",
                    afterSaleNo, parentClosed,
                    event.getPhysicalItems() == null ? 0 : event.getPhysicalItems().size());
            } catch (Exception e) {
                log.error("发送 AFTER_SALE_REFUNDED 失败 afterSaleNo={}", afterSaleNo, e);
            }
        });
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private AfterSaleRefundedEvent buildAfterSaleRefundedEvent(AfterSale afterSale, List<AfterSaleItem> afterItems, Order order, boolean parentClosed) {
        List<SkuRollbackItem> physicalItems = new ArrayList<>();
        if (CollUtil.isNotEmpty(afterItems)) {
            for (AfterSaleItem asi : afterItems) {
                physicalItems.add(SkuRollbackItem.builder()
                    .skuId(asi.getSkuId())
                    .buyCount(asi.getReturnQuantity())
                    .build());
            }
        }

        Map<Long, OrderItem> orderItemMap = loadOrderItemMap(afterSale.getTenantId(), afterSale.getOrderNo());
        // 收集要查专享的订单行 ID
        List<Long> candidateIds = new ArrayList<>();
        if (CollUtil.isNotEmpty(afterItems)) {
            for (AfterSaleItem asi : afterItems) {
                OrderItem oi = orderItemMap.get(asi.getOrderItemId());
                if (oi == null || (oi.getIsGift() != null && oi.getIsGift() == 1L)) {
                    continue;
                }
                candidateIds.add(oi.getId());
            }
        }
        // 这批行有没有挂秒杀/特价活动
        Map<Long, ExclusivePromoRef> exclusiveMap = orderPromoWriteService.loadExclusivePromos(afterSale.getTenantId(), candidateIds);
        List<SkuRollbackItem> exclusiveItems = new ArrayList<>();
        Long exclusiveActivityId = null;
        String exclusivePromoType = null;
        if (CollUtil.isNotEmpty(afterItems)) {
            for (AfterSaleItem asi : afterItems) {
                ExclusivePromoRef ref = exclusiveMap.get(asi.getOrderItemId());
                // 没有专享活动就跳过
                if (ref == null || ref.getActivityId() == null) {
                    continue;
                }
                OrderItem oi = orderItemMap.get(asi.getOrderItemId());
                if (oi == null) {
                    continue;
                }
                exclusiveItems.add(SkuRollbackItem.builder()
                    .skuId(oi.getSkuId())
                    .buyCount(asi.getReturnQuantity())
                    .build());
                if (exclusiveActivityId == null) {
                    exclusiveActivityId = ref.getActivityId();
                    exclusivePromoType = ref.getPromoType();
                }
            }
        }
        SharedPromoAssetsBo shared = null;
        if (parentClosed) {
            shared = orderPromoWriteService.loadSharedAssets(afterSale.getTenantId(), order.getParentOrderNo());
        }
        BigDecimal refundedAmountTotal = sumRefundedAmount(afterSale.getTenantId(), afterSale.getOrderNo());
        AfterSaleRefundedEvent.AfterSaleRefundedEventBuilder event = AfterSaleRefundedEvent.builder()
            .tenantId(afterSale.getTenantId())
            .channelCode(order.getChannelCode())
            .userId(afterSale.getUserinfoId())
            .orderNo(afterSale.getOrderNo())
            .parentOrderNo(order.getParentOrderNo())
            .afterSaleNo(afterSale.getAfterSaleNo())
            .payAmount(nullToZero(order.getPayAmount()))
            .refundAmount(nullToZero(afterSale.getRefundAmount()))
            .refundedAmountTotal(refundedAmountTotal)
            .physicalItems(physicalItems)
            .exclusiveItems(exclusiveItems)
            .activityId(exclusiveActivityId)
            .exclusivePromoType(exclusivePromoType)
            .rollbackSharedAssets(parentClosed);
        if (shared != null) {
            event.userCouponId(shared.getUserCouponId())
                .crossActivityId(shared.getCrossActivityId())
                .crossDiscountAmount(shared.getCrossDiscountAmount())
                .promoJoinActivityIds(shared.getPromoJoinActivityIds());
        }
        return event.build();
    }

    /**
     * 这个父单下，所有子单的购买数量是否都已经退完。
     */
    private boolean isParentFullyRefunded(String tenantId, String parentOrderNo) {
        // 查出父单下所有子单
        List<Order> siblings = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getParentOrderNo, parentOrderNo)
            .eq(Order::getDelFlag, "0"));
        if (CollUtil.isEmpty(siblings)) {
            return false;
        }
        for (Order sibling : siblings) {
            // 查该子单的订单明细
            List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getTenantId, tenantId)
                .eq(OrderItem::getOrderNo, sibling.getOrderNo()));
            if (CollUtil.isEmpty(items)) {
                continue;
            }
            for (OrderItem oi : items) {
                int buyQty = oi.getQuantity().intValue();
                int refundQty = nullToZero(oi.getRefundedQty());
                if (refundQty < buyQty) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<Long, OrderItem> loadOrderItemMap(String tenantId, String orderNo) {
        List<OrderItem> list = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        Map<Long, OrderItem> map = new HashMap<>();
        if (CollUtil.isNotEmpty(list)) {
            for (OrderItem oi : list) {
                map.put(oi.getId(), oi);
            }
        }
        return map;
    }

    /** 子单累计已退金额（行上 refundedAmount 之和） */
    private BigDecimal sumRefundedAmount(String tenantId, String orderNo) {
        List<OrderItem> list = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        BigDecimal sum = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(list)) {
            for (OrderItem oi : list) {
                sum = sum.add(nullToZero(oi.getRefundedAmount()));
            }
        }
        return sum;
    }

    /**
     * 售后成功退款之后，根据已成功退掉多少回写子单的支付状态
     */
    private void refreshOrderRefundFlags(String tenantId, String orderNo) {
        Order order = loadOrder(tenantId, orderNo);
        List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        int fullyRefundedLines = 0;
        for (OrderItem oi : orderItems) {
            if (nullToZero(oi.getRefundedQty()) == oi.getQuantity().intValue()) {
                fullyRefundedLines++;
            }
        }
        Long payStatus = fullyRefundedLines == orderItems.size() ? PAY_STATUS_FULL_REFUND : PAY_STATUS_PARTIAL_REFUND;
        order.setPayStatus(payStatus);
        // 已经全额退，而且订单还是 待发货，就把订单状态改成已取消
        if (PAY_STATUS_FULL_REFUND.equals(payStatus) && ORDER_STATUS_PENDING_SHIP.equals(order.getOrderStatus())) {
            order.setOrderStatus(ORDER_STATUS_CANCELLED);
        }
        orderMapper.updateById(order);
    }


    private int resolveOriginalPayCent(String tenantId, Order order) {
        List<Order> sameTrade = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getPayTradeNo, order.getPayTradeNo())
            .eq(Order::getDelFlag, "0"));
        BigDecimal sum = sameTrade.stream()
            .map(Order::getPayAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return toCent(sum);
    }

    private AfterSale loadAfterSale(String tenantId, String afterSaleNo) {
        if (StringUtils.isBlank(afterSaleNo)) {
            return null;
        }
        var q = Wrappers.<AfterSale>lambdaQuery().eq(AfterSale::getAfterSaleNo, afterSaleNo);
        if (StringUtils.isNotBlank(tenantId)) {
            q.eq(AfterSale::getTenantId, tenantId);
        }
        return afterSaleMapper.selectOne(q.last("LIMIT 1"));
    }

    private Order loadOrder(String tenantId, String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }
        return orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
    }

    private static int toCent(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private static int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
