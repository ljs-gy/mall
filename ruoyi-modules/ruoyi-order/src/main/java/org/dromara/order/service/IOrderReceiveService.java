package org.dromara.order.service;

/**
 * 确认收货（买家主动 / 超时自动）
 */
public interface IOrderReceiveService {

    /**
     * 买家确认收货
     */
    void receiveByBuyer(String tenantId, String orderNo, Long userinfoId);


    void receiveBySystem(String tenantId, String orderNo);
}
