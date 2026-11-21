package org.dromara.pm.service;

import org.dromara.order.api.mq.OrderPaidEvent;

/**
 * 订单支付成功后满额送券
 */
public interface IOrderPaidGiftGrantService {

    /**
     * 按父单实付匹配进行中的满赠规则并发券
     */
    void grantOnOrderPaid(OrderPaidEvent event);
}
