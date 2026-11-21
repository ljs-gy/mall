package org.dromara.order.service;

import org.dromara.order.api.domain.RemoteOrderPayConfirmBo;
import org.dromara.order.api.domain.RemotePayOrderSummary;

/**
 * 订单支付（子单 / 父单合并）
 */
public interface IOrderPayService {

    /**
     * 查询子单是否可支付
     */
    RemotePayOrderSummary getPayableOrder(String tenantId, Long userinfoId, String orderNo);

    /**
     * 查询父单合并支付信息
     */
    RemotePayOrderSummary getPayableParentOrder(String tenantId, Long userinfoId, String parentOrderNo);

    /**
     * 0 元子单确认支付
     */
    void confirmZeroPay(String tenantId, Long userinfoId, String orderNo);

    /**
     * 支付成功确认（子单）
     */
    boolean confirmOrderPaid(RemoteOrderPayConfirmBo bo);

    /**
     * 支付成功确认
     */
    boolean confirmParentOrderPaid(RemoteOrderPayConfirmBo bo);
}
