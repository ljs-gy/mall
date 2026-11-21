package org.dromara.pay.service;

import com.wechat.pay.java.service.refund.model.RefundNotification;

/**
 * 微信退款回调
 */
public interface IWxPayRefundNotifyService {

    void processRefundNotify(RefundNotification notification);
}
