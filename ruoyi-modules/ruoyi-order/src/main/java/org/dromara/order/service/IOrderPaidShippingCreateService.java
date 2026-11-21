package org.dromara.order.service;

import org.dromara.order.api.mq.OrderPaidEvent;

/**
 * 支付成功创建待发货发货单
 */
public interface IOrderPaidShippingCreateService {

    void createShippingOnPaid(OrderPaidEvent event);
}
