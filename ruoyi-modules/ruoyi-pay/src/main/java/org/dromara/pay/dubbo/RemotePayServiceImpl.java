package org.dromara.pay.dubbo;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.pay.api.RemotePayService;
import org.dromara.pay.api.domain.RemoteWxPayStatus;
import org.dromara.pay.api.domain.RemoteWxRefundBo;
import org.dromara.pay.api.domain.RemoteWxRefundResult;
import org.dromara.pay.service.IWxPayCloseService;
import org.dromara.pay.service.IWxPayQueryService;
import org.dromara.pay.service.IWxPayRefundService;

/**
 * 支付中心 Dubbo 服务
 */
@RequiredArgsConstructor
@DubboService
public class RemotePayServiceImpl implements RemotePayService {

    private final IWxPayCloseService wxPayCloseService;
    private final IWxPayQueryService wxPayQueryService;
    private final IWxPayRefundService wxPayRefundService;

    @Override
    public RemoteWxPayStatus queryWxPayStatus(String orderNo) {
        return wxPayQueryService.queryByOutTradeNo(orderNo);
    }

    @Override
    public void closeWxOrder(String orderNo) {
        wxPayCloseService.closeUnpaidOrder(orderNo);
    }

    @Override
    public RemoteWxRefundResult createRefund(RemoteWxRefundBo bo) {
        return wxPayRefundService.createRefund(bo);
    }

    @Override
    public RemoteWxRefundResult queryRefund(String outRefundNo) {
        return wxPayRefundService.queryByOutRefundNo(outRefundNo);
    }
}
