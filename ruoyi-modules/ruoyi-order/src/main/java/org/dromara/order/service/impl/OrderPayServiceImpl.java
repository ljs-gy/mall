package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.domain.RemoteOrderPayConfirmBo;
import org.dromara.order.api.domain.RemotePayOrderSummary;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.order.domain.Order;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPayServiceImpl implements IOrderPayService {

    private static final Long ORDER_STATUS_UNPAID = 10L;
    private static final Long ORDER_STATUS_PENDING_SHIP = 20L;
    private static final Long PAY_STATUS_UNPAID = 0L;
    private static final Long PAY_STATUS_PAID = 1L;

    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public RemotePayOrderSummary getPayableOrder(String tenantId, Long userinfoId, String orderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo) || userinfoId == null) {
            return null;
        }
        Order order = findOrder(tenantId, orderNo);
        if (order == null || !userinfoId.equals(order.getUserinfoId())) {
            return null;
        }
        RemotePayOrderSummary summary = new RemotePayOrderSummary();
        summary.setTenantId(tenantId);
        summary.setOrderNo(orderNo);
        summary.setParentOrderNo(order.getParentOrderNo());
        summary.setUserinfoId(userinfoId);
        summary.setPayAmount(order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount());
        summary.setPaid(PAY_STATUS_PAID.equals(order.getPayStatus()));
        summary.setUnpaid(ORDER_STATUS_UNPAID.equals(order.getOrderStatus())
            && PAY_STATUS_UNPAID.equals(order.getPayStatus()));
        return summary;
    }

    /**
     * 查「这个父单现在还能合并付多少钱」，给合单预下单用。
     */
    @Override
    public RemotePayOrderSummary getPayableParentOrder(String tenantId, Long userinfoId, String parentOrderNo) {
        List<Order> orders = listByParent(tenantId, parentOrderNo);
        if (CollUtil.isEmpty(orders)) {
            return null;
        }
        if (orders.stream().anyMatch(o -> !userinfoId.equals(o.getUserinfoId()))) {
            return null;
        }
        List<Order> unpaid = orders.stream().filter(this::isUnpaid).collect(Collectors.toList());
        BigDecimal unpaidSum = unpaid.stream()
            .map(o -> o.getPayAmount() == null ? BigDecimal.ZERO : o.getPayAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        RemotePayOrderSummary summary = new RemotePayOrderSummary();
        summary.setTenantId(tenantId);
        summary.setParentOrderNo(parentOrderNo);
        summary.setUserinfoId(userinfoId);
        summary.setPayAmount(unpaidSum);
        summary.setUnpaid(CollUtil.isNotEmpty(unpaid));
        summary.setPaid(CollUtil.isEmpty(unpaid) && orders.stream().anyMatch(o -> PAY_STATUS_PAID.equals(o.getPayStatus())));
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmZeroPay(String tenantId, Long userinfoId, String orderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo) || userinfoId == null) {
            throw new ServiceException("参数不完整");
        }
        RemotePayOrderSummary summary = getPayableOrder(tenantId, userinfoId, orderNo);
        if (summary == null) {
            throw new ServiceException("订单不存在或无权支付");
        }
        if (Boolean.TRUE.equals(summary.getPaid())) {
            log.info("0元确认幂等跳过，子单已支付 orderNo={}", orderNo);
            return;
        }
        if (!Boolean.TRUE.equals(summary.getUnpaid())) {
            throw new ServiceException("订单状态不可支付");
        }
        BigDecimal total = summary.getPayAmount() == null ? BigDecimal.ZERO : summary.getPayAmount();
        if (total.compareTo(BigDecimal.ZERO) != 0) {
            throw new ServiceException("非0元订单请走微信支付");
        }
        RemoteOrderPayConfirmBo confirmBo = new RemoteOrderPayConfirmBo();
        confirmBo.setTenantId(tenantId);
        confirmBo.setOrderNo(orderNo);
        confirmBo.setParentOrderNo(summary.getParentOrderNo());
        confirmBo.setThirdTradeNo("ZERO-" + orderNo);
        confirmBo.setPayAmountCent(0);
        confirmBo.setPayTime(new Date());
        if (!confirmOrderPaid(confirmBo)) {
            throw new ServiceException("0元订单确认失败，请稍后重试");
        }
        log.info("0元子单确认支付成功 orderNo={}", orderNo);
    }

    /**
     * 单店支付确认付款：微信商户单号是子单号时走这里，只确认这一张子单。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrderPaid(RemoteOrderPayConfirmBo bo) {
        Order order = findOrder(bo.getTenantId(), bo.getOrderNo());
        if (order == null) {
            log.error("支付回调子单不存在 orderNo={}", bo.getOrderNo());
            return false;
        }
        Date payTime = bo.getPayTime();
        if (PAY_STATUS_PAID.equals(order.getPayStatus())) {
            log.info("子单已支付，幂等跳过 orderNo={}", bo.getOrderNo());
            return true;
        }
        if (!ORDER_STATUS_UNPAID.equals(order.getOrderStatus())
            || !PAY_STATUS_UNPAID.equals(order.getPayStatus())) {
            log.warn("子单非待支付，拒绝确认 orderNo={} status={} payStatus={}",
                bo.getOrderNo(), order.getOrderStatus(), order.getPayStatus());
            return false;
        }
        int dbCent = toCent(order.getPayAmount());
        if (bo.getPayAmountCent() != null && !Objects.equals(bo.getPayAmountCent(), dbCent)) {
            log.error("支付金额不一致 orderNo={} wx={}分 db={}分",
                bo.getOrderNo(), bo.getPayAmountCent(), dbCent);
            throw new ServiceException("支付金额校验未通过");
        }
        int updated = orderMapper.markPaidByOrderNo(
            bo.getTenantId(),
            bo.getOrderNo(),
            ORDER_STATUS_UNPAID,
            PAY_STATUS_UNPAID,
            ORDER_STATUS_PENDING_SHIP,
            PAY_STATUS_PAID,
            payTime,
            StringUtils.blankToDefault(bo.getThirdTradeNo(), ""));
        if (updated <= 0) {
            log.warn("子单支付未翻转 orderNo={}", bo.getOrderNo());
            return false;
        }
        publishOrderPaidAfterCommit(order, payTime);
        log.info("子单支付成功 orderNo={} thirdTradeNo={}", bo.getOrderNo(), bo.getThirdTradeNo());
        return true;
    }

    /**
     * 合并支付确认付款
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmParentOrderPaid(RemoteOrderPayConfirmBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getParentOrderNo()) || StringUtils.isBlank(bo.getTenantId())) {
            return false;
        }
        String parentOrderNo = bo.getParentOrderNo();
        // 查出父单下所有子单
        List<Order> orders = listByParent(bo.getTenantId(), parentOrderNo);
        if (CollUtil.isEmpty(orders)) {
            log.error("合并支付回调父单不存在 parentOrderNo={}", parentOrderNo);
            return false;
        }
        Date payTime = bo.getPayTime();
        // 筛出仍未支付的子单
        List<Order> unpaid = orders.stream().filter(this::isUnpaid).collect(Collectors.toList());
        if (CollUtil.isEmpty(unpaid)) {
            boolean allPaid = orders.stream().allMatch(o -> PAY_STATUS_PAID.equals(o.getPayStatus()));
            if (allPaid) {
                log.info("合并支付幂等：父单下子单均已支付 parentOrderNo={}", parentOrderNo);
                return true;
            }
            log.warn("合并支付无未付子单且非全已付 parentOrderNo={}", parentOrderNo);
            return false;
        }
        int dbCent = unpaid.stream().mapToInt(o -> toCent(o.getPayAmount())).sum();
        if (bo.getPayAmountCent() != null && !Objects.equals(bo.getPayAmountCent(), dbCent)) {
            log.error("合并支付金额不一致 parentOrderNo={} wx={}分 db={}分",
                parentOrderNo, bo.getPayAmountCent(), dbCent);
            throw new ServiceException("支付金额校验未通过");
        }
        String tradeNo = bo.getThirdTradeNo();
        boolean anyPaid = false;
        for (Order order : unpaid) {
            int updated = orderMapper.markPaidByOrderNo(
                bo.getTenantId(),
                order.getOrderNo(),
                ORDER_STATUS_UNPAID,
                PAY_STATUS_UNPAID,
                ORDER_STATUS_PENDING_SHIP,
                PAY_STATUS_PAID,
                payTime,
                tradeNo);
            if (updated > 0) {
                anyPaid = true;
                publishOrderPaidAfterCommit(order, payTime);
            }
        }
        if (!anyPaid) {
            log.warn("合并支付未翻转子单 parentOrderNo={}", parentOrderNo);
            return false;
        }
        log.info("合并支付成功 parentOrderNo={} thirdTradeNo={}", parentOrderNo, bo.getThirdTradeNo());
        return true;
    }

    private boolean isUnpaid(Order order) {
        return ORDER_STATUS_UNPAID.equals(order.getOrderStatus())
            && PAY_STATUS_UNPAID.equals(order.getPayStatus());
    }

    /**
     * 本地确认支付成功后，等当前事务提交完，再发 ORDER_PAID 支付成功广播。
     */
    private void publishOrderPaidAfterCommit(Order order, Date payTime) {
        BigDecimal paidAmount = order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();
        OrderPaidEvent event = OrderPaidEvent.builder()
            .tenantId(order.getTenantId())
            .channelCode(order.getChannelCode())
            .memberCode(order.getMemberCode())
            .userId(order.getUserinfoId())
            .userLevelId(order.getUserLevelId())
            .orderNo(order.getOrderNo())
            .parentOrderNo(order.getParentOrderNo())
            .paidAmount(paidAmount)
            .payTime(payTime)
            .build();
        afterCommit(() -> {
            try {
                rabbitTemplate.convertAndSend(
                    OrderMqConstants.EXCHANGE_ORDER_PAID,
                    OrderMqConstants.KEY_ORDER_PAID,
                    event);
                log.info("已发送 ORDER_PAID orderNo={} paidAmount={}", order.getOrderNo(), paidAmount);
            } catch (Exception e) {
                log.error("发送 ORDER_PAID 失败 orderNo={}", order.getOrderNo(), e);
            }
        });
    }

    private List<Order> listByParent(String tenantId, String parentOrderNo) {
        return orderMapper.selectList(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getParentOrderNo, parentOrderNo)
            .eq(Order::getDelFlag, "0"));
    }

    private int toCent(BigDecimal yuan) {
        BigDecimal amount = yuan == null ? BigDecimal.ZERO : yuan;
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
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

    private Order findOrder(String tenantId, String orderNo) {
        return orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
    }
}
