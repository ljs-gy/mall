package org.dromara.dis.service;

import org.dromara.order.api.mq.AfterSaleRefundedEvent;

/**
 * 售后退款后重算待结算佣金
 */
public interface IDistCommissionRefundService {

    /**
     * 按剩余可计佣金额（实付 − 累计已退）重算待结算明细；剩 0 则取消
     */
    void recalcOnRefunded(AfterSaleRefundedEvent event);
}
