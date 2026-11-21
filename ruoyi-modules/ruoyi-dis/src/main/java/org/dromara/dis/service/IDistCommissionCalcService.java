package org.dromara.dis.service;

import org.dromara.order.api.mq.OrderPaidEvent;

/**
 * 支付成功后计算分销待结算佣金
 */
public interface IDistCommissionCalcService {

    /**
     * 按已支付订单生成分佣头+明细（待结算），幂等
     */
    void createPendingOnPaid(OrderPaidEvent event);
}
