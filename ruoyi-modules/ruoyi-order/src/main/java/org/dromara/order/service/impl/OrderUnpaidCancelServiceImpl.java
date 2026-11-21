package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.order.api.mq.OrderCancelledEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.SkuRollbackItem;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.bo.CancelUnpaidCommand;
import org.dromara.order.domain.bo.CancelUnpaidCommand.UnpaidCancelOperatorType;
import org.dromara.order.domain.bo.ExclusivePromoRef;
import org.dromara.order.domain.bo.SharedPromoAssetsBo;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderCancelCompensateService;
import org.dromara.order.service.IOrderPromoWriteService;
import org.dromara.order.service.IOrderUnpaidCancelService;
import org.dromara.pay.api.RemotePayService;
import org.dromara.pay.api.domain.RemoteWxPayStatus;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 未支付子单取消
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUnpaidCancelServiceImpl implements IOrderUnpaidCancelService {

    private static final long ORDER_STATUS_UNPAID = 10L;
    private static final long ORDER_STATUS_CANCELLED = 50L;
    private static final long PAY_STATUS_UNPAID = 0L;

    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 30L;

    private final RedissonClient redissonClient;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final IOrderCancelCompensateService orderCancelCompensateService;
    private final IOrderPromoWriteService orderPromoWriteService;
    private final RabbitTemplate rabbitTemplate;

    @Lazy
    @Autowired
    private OrderUnpaidCancelServiceImpl self;

    @DubboReference
    private RemotePayService remotePayService;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;


    @Override
    public void cancelUnpaid(CancelUnpaidCommand command) {
        validateCommand(command);
        String parentOrderNo = requireParentOrderNo(command.getTenantId(), command.getOrderNo());
        RLock lock = redissonClient.getLock(OrderCacheKeys.payChannelLock(parentOrderNo));
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new ServiceException("正在处理支付或取消，请稍后重试");
            }
            try {
                doManualCancelUnderLock(command, parentOrderNo);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("取消处理中断，请稍后重试");
        }
    }

    @Override
    public void cancelOnTimeout(String tenantId, String orderNo) {
        String parentOrderNo = findParentOrderNo(tenantId, orderNo);
        if (StringUtils.isBlank(parentOrderNo)) {
            log.warn("超时关单订单不存在 orderNo={}", orderNo);
            return;
        }
        RLock lock = redissonClient.getLock(OrderCacheKeys.payChannelLock(parentOrderNo));
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new ServiceException("正在处理支付或取消，请稍后重试");
            }
            try {
                doTimeoutCancelUnderLock(tenantId, orderNo, parentOrderNo);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("取消处理中断，请稍后重试");
        }
    }

    private void doManualCancelUnderLock(CancelUnpaidCommand command, String parentOrderNo) {
        String tenantId = command.getTenantId();
        String orderNo = command.getOrderNo();
        Order latest = findOrder(tenantId, orderNo);
        if (latest == null) {
            throw new ServiceException("订单不存在");
        }
        assertOperatorPermission(command, latest);
        if (orderCancelCompensateService.isCancelCompensated(tenantId, orderNo)) {
            return;
        }
        if (ORDER_STATUS_CANCELLED == latest.getOrderStatus()) {
            compensateAlreadyCancelled(latest);
            return;
        }
        if (!isUnpaid(latest)) {
            throw new ServiceException("订单状态不可取消");
        }
        forbidCancelIfWxBusy(orderNo);
        if (!orderNo.equals(parentOrderNo)) {
            forbidCancelIfWxBusy(parentOrderNo);
        }
        if (!self.markCancelledAndReversePromo(tenantId, parentOrderNo, orderNo)) {
            throw new ServiceException("订单已支付，无法取消");
        }
        finishCancel(latest, tenantId, orderNo, parentOrderNo);
    }

    private void doTimeoutCancelUnderLock(String tenantId, String orderNo, String parentOrderNo) {
        if (orderCancelCompensateService.isCancelCompensated(tenantId, orderNo)) {
            return;
        }
        Order latest = findOrder(tenantId, orderNo);
        if (latest == null) {
            log.warn("超时关单锁内订单不存在 orderNo={}", orderNo);
            return;
        }
        if (ORDER_STATUS_CANCELLED == latest.getOrderStatus()) {
            compensateAlreadyCancelled(latest);
            return;
        }
        if (!isUnpaid(latest)) {
            log.info("超时关单锁内发现非未支付，跳过 orderNo={} orderStatus={} payStatus={}",
                orderNo, latest.getOrderStatus(), latest.getPayStatus());
            return;
        }
        // 关单前问微信：这笔单现在能不能关。超时关单不能把已支付改成取消
        if (shouldSkipTimeoutForWx(orderNo) || (!orderNo.equals(parentOrderNo) && shouldSkipTimeoutForWx(parentOrderNo))) {
            return;
        }
        if (!self.markCancelledAndReversePromo(tenantId, parentOrderNo, orderNo)) {
            log.info("超时关单 CAS 未抢到，跳过 orderNo={}", orderNo);
            return;
        }
        finishCancel(latest, tenantId, orderNo, parentOrderNo);
    }

    private void finishCancel(Order latest, String tenantId, String orderNo, String parentOrderNo) {
        closeWxQuietly(orderNo);
        closeWxQuietly(parentOrderNo);
        boolean rollbackShared = orderCancelCompensateService.allSiblingsCancelled(tenantId, parentOrderNo);
        List<OrderItem> items = loadOrderItems(tenantId, orderNo);
        rollbackPhysicalStock(items);
        publishPromoRollback(latest, items, rollbackShared);
        orderCancelCompensateService.markCancelCompensated(tenantId, orderNo);
        log.info("未支付取消成功 orderNo={} rollbackShared={}", orderNo, rollbackShared);
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean markCancelledAndReversePromo(String tenantId, String parentOrderNo, String orderNo) {
        if (orderMapper.cancelUnpaidByOrderNo(
            tenantId, orderNo, ORDER_STATUS_UNPAID, PAY_STATUS_UNPAID, ORDER_STATUS_CANCELLED) <= 0) {
            return false;
        }
        orderPromoWriteService.writeReverseOnCancel(tenantId, parentOrderNo, orderNo);
        if (orderCancelCompensateService.allSiblingsCancelled(tenantId, parentOrderNo)) {
            int rows = orderPromoWriteService.casReleaseSharedAssets(tenantId, parentOrderNo);
            log.info("未支付取消父单完结 CAS 共享资产 parentOrderNo={} rows={}", parentOrderNo, rows);
        }
        return true;
    }


    private void forbidCancelIfWxBusy(String outTradeNo) {
        RemoteWxPayStatus wx = requireWxStatus(outTradeNo);
        if (RemoteWxPayStatus.SUCCESS.equals(wx.getTradeState())) {
            throw new ServiceException("订单已完成支付，请稍后刷新查看");
        }
        if (RemoteWxPayStatus.USERPAYING.equals(wx.getTradeState())) {
            throw new ServiceException("订单正在支付中，暂无法取消，请稍后再试");
        }
    }

    private boolean shouldSkipTimeoutForWx(String outTradeNo) {
        RemoteWxPayStatus wx = requireWxStatus(outTradeNo);
        if (RemoteWxPayStatus.SUCCESS.equals(wx.getTradeState())) {
            log.info("超时关单检测到微信支付已成功，跳过关单 outTradeNo={}", outTradeNo);
            return true;
        }
        if (RemoteWxPayStatus.USERPAYING.equals(wx.getTradeState())) {
            throw new ServiceException("用户支付中，暂不关单 outTradeNo=" + outTradeNo);
        }
        return false;
    }

    private RemoteWxPayStatus requireWxStatus(String outTradeNo) {
        RemoteWxPayStatus wx = remotePayService.queryWxPayStatus(outTradeNo);
        if (wx == null) {
            throw new ServiceException("查询微信支付状态失败，请稍后重试");
        }
        return wx;
    }

    private void compensateAlreadyCancelled(Order order) {
        if (orderCancelCompensateService.isCancelCompensated(order.getTenantId(), order.getOrderNo())) {
            return;
        }
        // 本地已经取消了，让微信侧对应的预支付单也关掉，避免用户还能扫码付成功。
        // 合单场景下两边都可能下过微信单，所以两个都关。
        // 取消的是子单，但：若只关子单号 → 合单码可能还能付，把这笔子单一起付掉，若只关父单号 → 单店码可能还能付，所以两行都关：把可能还开着的两条支付通道都堵上。
        closeWxQuietly(order.getOrderNo());
        closeWxQuietly(order.getParentOrderNo());
        boolean rollbackShared = orderCancelCompensateService.allSiblingsCancelled(order.getTenantId(), order.getParentOrderNo());
        orderPromoWriteService.writeReverseOnCancel(order.getTenantId(), order.getParentOrderNo(), order.getOrderNo());
        // 这个父单下所有子单都已经取消了，把父单营销主表里还处在「已生效」的券、跨店满减改成「已释放」。
        if (rollbackShared) {
            orderPromoWriteService.casReleaseSharedAssets(order.getTenantId(), order.getParentOrderNo());
        }
        List<OrderItem> items = loadOrderItems(order.getTenantId(), order.getOrderNo());
        rollbackPhysicalStock(items);
        publishPromoRollback(order, items, rollbackShared);
        orderCancelCompensateService.markCancelCompensated(order.getTenantId(), order.getOrderNo());
        log.warn("子单已取消，补发取消善后 orderNo={}", order.getOrderNo());
    }

    private List<OrderItem> loadOrderItems(String tenantId, String orderNo) {
        return orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
    }

    private void rollbackPhysicalStock(List<OrderItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (OrderItem item : items) {
            remoteGoodsService.rollbackPhysicalStock(item.getSkuId(), item.getQuantity().intValue());
        }
    }

    /**
     * 营销还专享 / 共享资产（Redis），失败由队列重试
     */
    private void publishPromoRollback(Order order, List<OrderItem> items, boolean rollbackShared) {
        List<Long> exclusiveCandidateIds = new ArrayList<>();
        List<SkuRollbackItem> exclusiveItems = new ArrayList<>();
        if (CollUtil.isNotEmpty(items)) {
            for (OrderItem item : items) {
                if (item.getIsGift() != null && item.getIsGift() == 1L) {
                    continue;
                }
                exclusiveCandidateIds.add(item.getId());
            }
        }
        Map<Long, ExclusivePromoRef> exclusiveMap = orderPromoWriteService.loadExclusivePromos(order.getTenantId(), exclusiveCandidateIds);
        Long exclusiveActivityId = null;
        String exclusivePromoType = null;
        if (CollUtil.isNotEmpty(items)) {
            for (OrderItem item : items) {
                if (item.getIsGift() != null && item.getIsGift() == 1L) {
                    continue;
                }
                ExclusivePromoRef ref = exclusiveMap.get(item.getId());
                if (ref == null || ref.getActivityId() == null) {
                    continue;
                }
                exclusiveItems.add(SkuRollbackItem.builder()
                    .skuId(item.getSkuId())
                    .buyCount(item.getQuantity().intValue())
                    .build());
                if (exclusiveActivityId == null) {
                    exclusiveActivityId = ref.getActivityId();
                    exclusivePromoType = ref.getPromoType();
                }
            }
        }

        SharedPromoAssetsBo shared = rollbackShared
            ? orderPromoWriteService.loadSharedAssets(order.getTenantId(), order.getParentOrderNo())
            : null;
        if (CollUtil.isEmpty(exclusiveItems) && !rollbackShared) {
            return;
        }
        OrderCancelledEvent.OrderCancelledEventBuilder cancelled = OrderCancelledEvent.builder()
            .tenantId(order.getTenantId())
            .channelCode(order.getChannelCode())
            .userId(order.getUserinfoId())
            .orderNo(order.getOrderNo())
            .parentOrderNo(order.getParentOrderNo())
            .exclusiveItems(exclusiveItems)
            .activityId(exclusiveActivityId)
            .exclusivePromoType(exclusivePromoType)
            .rollbackSharedAssets(rollbackShared);
        if (shared != null) {
            cancelled.userCouponId(shared.getUserCouponId())
                .crossActivityId(shared.getCrossActivityId())
                .crossDiscountAmount(shared.getCrossDiscountAmount())
                .promoJoinActivityIds(shared.getPromoJoinActivityIds());
        }
        rabbitTemplate.convertAndSend(OrderMqConstants.EXCHANGE_ORDER_CANCELLED, "", cancelled.build());
        log.info("已发送 ORDER_CANCELLED 营销回滚 orderNo={} rollbackShared={}", order.getOrderNo(), rollbackShared);
    }

    /**
     * 校验用户
     */
    private void assertOperatorPermission(CancelUnpaidCommand command, Order order) {
        UnpaidCancelOperatorType type = command.getOperatorType();
        if (type == UnpaidCancelOperatorType.OPS) {
            return;
        }
        if (type == UnpaidCancelOperatorType.BUYER) {
            if (StringUtils.isBlank(command.getOperatorId())
                || !command.getOperatorId().equals(order.getMemberBcode())) {
                throw new ServiceException("无权取消该订单");
            }
            return;
        }
        if (type == UnpaidCancelOperatorType.MERCHANT) {
            if (StringUtils.isBlank(command.getOperatorId())
                || !command.getOperatorId().equals(order.getMemberCode())) {
                throw new ServiceException("无权取消该店铺订单");
            }
            return;
        }
        throw new ServiceException("不支持的取消操作类型");
    }

    private void validateCommand(CancelUnpaidCommand command) {
        if (command == null || StringUtils.isBlank(command.getTenantId()) || StringUtils.isBlank(command.getOrderNo())
            || command.getOperatorType() == null || command.getOperatorType() == UnpaidCancelOperatorType.SYSTEM_TIMEOUT) {
            throw new ServiceException("取消参数不完整");
        }
    }

    private void closeWxQuietly(String outTradeNo) {
        if (StringUtils.isBlank(outTradeNo)) {
            return;
        }
        try {
            remotePayService.closeWxOrder(outTradeNo);
        } catch (Exception e) {
            log.warn("微信关单异常(忽略) outTradeNo={}", outTradeNo, e);
        }
    }

    private Order findOrder(String tenantId, String orderNo) {
        return orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
    }

    private String requireParentOrderNo(String tenantId, String orderNo) {
        String parentOrderNo = findParentOrderNo(tenantId, orderNo);
        if (StringUtils.isBlank(parentOrderNo)) {
            throw new ServiceException("订单不存在");
        }
        return parentOrderNo;
    }

    private String findParentOrderNo(String tenantId, String orderNo) {
        Order row = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .select(Order::getParentOrderNo)
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
        return row == null ? null : row.getParentOrderNo();
    }

    private boolean isUnpaid(Order order) {
        return ORDER_STATUS_UNPAID == order.getOrderStatus()
            && PAY_STATUS_UNPAID == order.getPayStatus();
    }
}
