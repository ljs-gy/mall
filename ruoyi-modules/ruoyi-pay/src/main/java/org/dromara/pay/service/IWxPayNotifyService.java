package org.dromara.pay.service;

import com.wechat.pay.java.service.payments.model.Transaction;

/**
 * 微信支付回调处理
 */
public interface IWxPayNotifyService {

    /**
     * @return true 已确认或幂等成功 / 非 SUCCESS 状态可忽略；false 需让微信重试
     */
    boolean processPaymentSuccess(Transaction transaction);
}
