package org.dromara.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.Order;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.mapper.ShippingOrderMapper;
import org.dromara.order.service.IOrderCompleteService;
import org.dromara.order.service.IOrderReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.Date;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReceiveServiceImpl implements IOrderReceiveService {

    private static final Long ORDER_STATUS_PENDING_RECEIVE = 30L;
    private static final Long ORDER_STATUS_COMPLETED = 40L;
    private static final Long SHIPPING_STATUS_SHIPPED = 20L;
    private static final Long SHIPPING_STATUS_RECEIVED = 30L;

    /** 在途售后：待审核 / 待退货 / 待验货 / 退款中 */
    private static final Long AS_PENDING_AUDIT = 10L;
    private static final Long AS_WAIT_RETURN = 20L;
    private static final Long AS_WAIT_INSPECT = 30L;
    private static final Long AS_REFUNDING = 40L;

    private final OrderMapper orderMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final ShippingOrderMapper shippingOrderMapper;
    private final IOrderCompleteService orderCompleteService;

    @Lazy
    @Autowired
    private OrderReceiveServiceImpl self;

    @Override
    public void receiveByBuyer(String tenantId, String orderNo, Long userinfoId) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo) || userinfoId == null) {
            throw new ServiceException("参数不完整");
        }
        self.confirmReceive(tenantId, orderNo, userinfoId, true);
    }

    @Override
    public void receiveBySystem(String tenantId, String orderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo)) {
            log.warn("自动确认收货参数不完整 tenantId={} orderNo={}", tenantId, orderNo);
            return;
        }
        try {
            self.confirmReceive(tenantId, orderNo, null, false);
        } catch (ServiceException e) {
            // 售后中等可恢复场景：跳过，不抛出以免延时消息疯狂重试
            log.warn("自动确认收货跳过 orderNo={} reason={}", orderNo, e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(String tenantId, String orderNo, Long userinfoId, boolean buyer) {
        // 事务内锁订单行，与售后申请互斥
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1 FOR UPDATE"));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (buyer && (userinfoId == null || !userinfoId.equals(order.getUserinfoId()))) {
            throw new ServiceException("无权操作该订单");
        }
        if (ORDER_STATUS_COMPLETED.equals(order.getOrderStatus())) {
            log.info("订单已确认收货，幂等跳过 orderNo={} buyer={}", orderNo, buyer);
            return;
        }
        if (!ORDER_STATUS_PENDING_RECEIVE.equals(order.getOrderStatus())) {
            throw new ServiceException("订单非待收货状态，无法确认收货");
        }
        if (hasInTransitAfterSale(tenantId, orderNo)) {
            throw new ServiceException("订单售后处理中，暂不可确认收货");
        }

        Date now = new Date();
        int orderRows = orderMapper.markReceivedByOrderNo(
            tenantId, orderNo,
            ORDER_STATUS_PENDING_RECEIVE, ORDER_STATUS_COMPLETED, now);
        if (orderRows <= 0) {
            throw new ServiceException("确认收货失败，订单状态已变更");
        }

        int shipRows = shippingOrderMapper.receiveByOrderNoCas(
            tenantId, orderNo,
            SHIPPING_STATUS_SHIPPED, SHIPPING_STATUS_RECEIVED, now);
        if (shipRows <= 0) {
            log.warn("确认收货时发货单未更新(可能无已发货单) orderNo={}", orderNo);
        }
        log.info("确认收货成功 orderNo={} buyer={}", orderNo, buyer);

        String channelCode = order.getChannelCode();
        afterCommit(() -> orderCompleteService.scheduleCompleteDelay(tenantId, channelCode, orderNo));
    }

    private boolean hasInTransitAfterSale(String tenantId, String orderNo) {
        Long cnt = afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getOrderNo, orderNo)
            .in(AfterSale::getStatus, Arrays.asList(
                AS_PENDING_AUDIT, AS_WAIT_RETURN, AS_WAIT_INSPECT, AS_REFUNDING)));
        return cnt != null && cnt > 0;
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
}
