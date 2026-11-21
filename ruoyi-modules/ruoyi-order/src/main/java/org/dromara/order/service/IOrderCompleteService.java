package org.dromara.order.service;

/**
 * 确认收货后过售后期 → 交易完成
 */
public interface IOrderCompleteService {

    /**
     * 确认收货成功后投递「过售后期」延时消息
     */
    void scheduleCompleteDelay(String tenantId, String channelCode, String orderNo);

    /**
     * 延时到期：校验后广播 ORDER_COMPLETED；售后中则再次延时
     */
    void onCompleteDelay(String tenantId, String channelCode, String orderNo);
}
