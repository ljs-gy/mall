package org.dromara.dis.service;

import org.dromara.order.api.mq.OrderCompletedEvent;

/**
 * 交易完成后分销佣金入账
 */
public interface IDistCommissionSettleService {

    /**
     * 将订单下待结算明细入账 COMMISSION 账户
     */
    void settleOnCompleted(OrderCompletedEvent event);
}
