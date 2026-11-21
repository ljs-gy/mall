package org.dromara.pay.service;

/**
 * 微信支付关单
 */
public interface IWxPayCloseService {

    /**
     * 查询微信订单状态后关闭未支付订单
     */
    void closeUnpaidOrder(String parentOrderNo);
}
