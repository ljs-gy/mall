package org.dromara.pay.service;

import org.dromara.pay.api.domain.RemoteWxPayStatus;

/**
 * 微信支付查单
 */
public interface IWxPayQueryService {

    /**
     * 按商户订单号查询微信支付状态
     */
    RemoteWxPayStatus queryByOutTradeNo(String parentOrderNo);
}
