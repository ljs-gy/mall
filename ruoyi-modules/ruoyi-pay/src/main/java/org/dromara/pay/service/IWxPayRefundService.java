package org.dromara.pay.service;

import org.dromara.pay.api.domain.RemoteWxRefundBo;
import org.dromara.pay.api.domain.RemoteWxRefundResult;

/**
 * 微信退款
 */
public interface IWxPayRefundService {

    RemoteWxRefundResult createRefund(RemoteWxRefundBo bo);

    RemoteWxRefundResult queryByOutRefundNo(String outRefundNo);
}
