package org.dromara.order.dubbo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.RemoteAfterSaleRefundConfirmBo;
import org.dromara.order.api.domain.RemoteOrderCreateBo;
import org.dromara.order.api.domain.RemoteOrderItemCreateBo;
import org.dromara.order.api.domain.RemoteOrderPayConfirmBo;
import org.dromara.order.api.domain.RemotePayOrderSummary;
import org.dromara.order.domain.CartItem;
import org.dromara.order.domain.Order;
import org.dromara.order.mapper.CartItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IAfterSaleRefundService;
import org.dromara.order.service.IOrderPayService;
import org.dromara.order.service.IOrderWriteService;

import java.util.Collection;
import java.util.List;

/**
 * 订单中心 Dubbo 服务
 */
@Slf4j
@RequiredArgsConstructor
@DubboService
public class RemoteOrderServiceImpl implements RemoteOrderService {

    private static final Long PAY_STATUS_UNPAID = 0L;
    private static final Long ORDER_STATUS_CANCELLED = 50L;

    private final OrderMapper orderMapper;
    private final CartItemMapper cartItemMapper;
    private final IOrderPayService orderPayService;
    private final IOrderWriteService orderWriteService;
    private final IAfterSaleRefundService afterSaleRefundService;

    @Override
    public Long createOrder(RemoteOrderCreateBo bo) {
        return orderWriteService.createOrder(bo);
    }

    @Override
    public Long createOrderItem(RemoteOrderItemCreateBo bo) {
        return orderWriteService.createOrderItem(bo);
    }

    @Override
    public int deleteCartItems(String tenantId, Long userinfoId, Collection<Long> cartItemIds) {
        if (CollUtil.isEmpty(cartItemIds) || userinfoId == null || StringUtils.isBlank(tenantId)) {
            return 0;
        }
        return cartItemMapper.delete(Wrappers.<CartItem>lambdaQuery()
            .eq(CartItem::getTenantId, tenantId)
            .eq(CartItem::getUserinfoId, userinfoId)
            .in(CartItem::getId, cartItemIds));
    }

    @Override
    public int cancelUnpaidByOrderNo(String tenantId, String orderNo, Long fromStatus, Long toStatus) {
        return orderMapper.cancelUnpaidByOrderNo(tenantId, orderNo, fromStatus, PAY_STATUS_UNPAID, toStatus);
    }

    @Override
    public RemotePayOrderSummary getPayableOrder(String tenantId, Long userinfoId, String orderNo) {
        return orderPayService.getPayableOrder(tenantId, userinfoId, orderNo);
    }

    @Override
    public RemotePayOrderSummary getPayableParentOrder(String tenantId, Long userinfoId, String parentOrderNo) {
        return orderPayService.getPayableParentOrder(tenantId, userinfoId, parentOrderNo);
    }

    @Override
    public boolean confirmOrderPaid(RemoteOrderPayConfirmBo bo) {
        return orderPayService.confirmOrderPaid(bo);
    }

    @Override
    public boolean confirmParentOrderPaid(RemoteOrderPayConfirmBo bo) {
        return orderPayService.confirmParentOrderPaid(bo);
    }

    @Override
    public boolean confirmAfterSaleRefunded(RemoteAfterSaleRefundConfirmBo bo) {
        return afterSaleRefundService.confirmRefunded(bo);
    }

    @Override
    public boolean allSubOrdersCancelled(String tenantId, String parentOrderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(parentOrderNo)) {
            return false;
        }
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getParentOrderNo, parentOrderNo)
            .eq(Order::getDelFlag, "0"));
        if (CollUtil.isEmpty(orders)) {
            return false;
        }
        return orders.stream().allMatch(o -> ORDER_STATUS_CANCELLED.equals(o.getOrderStatus()));
    }
}
