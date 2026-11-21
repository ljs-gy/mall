package org.dromara.order.service;

import org.dromara.order.domain.bo.CancelUnpaidCommand;

/**
 * 未支付子单取消
 */
public interface IOrderUnpaidCancelService {

    /**
     * 买家/商家/运营取消未支付子单。
     */
    void cancelUnpaid(CancelUnpaidCommand command);

    /**
     * 超时延时 MQ 关未支付子单
     */
    void cancelOnTimeout(String tenantId, String orderNo);
}
