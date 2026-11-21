package org.dromara.pay.service.impl;

import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.pay.api.domain.RemoteWxRefundBo;
import org.dromara.pay.api.domain.RemoteWxRefundResult;
import org.dromara.pay.config.WeChatPayProperties;
import org.dromara.pay.service.IWxPayRefundService;
import org.springframework.stereotype.Service;

/**
 * 微信退款：申请 / 查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayRefundServiceImpl implements IWxPayRefundService {

    private final RefundService refundService;
    private final WeChatPayProperties wxPayProperties;

    @Override
    public RemoteWxRefundResult createRefund(RemoteWxRefundBo bo) {
        AmountReq amount = new AmountReq();
        amount.setRefund(bo.getRefundAmountCent());
        amount.setTotal(bo.getTotalAmountCent());
        amount.setCurrency("CNY");
        CreateRequest request = new CreateRequest();
        request.setTransactionId(bo.getTransactionId());
        request.setOutRefundNo(bo.getOutRefundNo());
        request.setReason(bo.getReason());
        request.setAmount(amount);
        String notifyUrl = resolveRefundNotifyUrl();
        request.setNotifyUrl(notifyUrl);
        try {
            Refund refund = refundService.create(request);
            RemoteWxRefundResult result = toResult(refund);
            log.info("微信退款已发起 outRefundNo={} status={} refundId={}", result.getOutRefundNo(), result.getStatus(), result.getRefundId());
            return result;
        } catch (Exception e) {
            log.error("微信退款申请失败 outRefundNo={}", bo.getOutRefundNo(), e);
            throw new ServiceException("微信退款申请失败: " + e.getMessage());
        }
    }

    @Override
    public RemoteWxRefundResult queryByOutRefundNo(String outRefundNo) {
        if (StringUtils.isBlank(outRefundNo)) {
            throw new ServiceException("退款单号不能为空");
        }
        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(outRefundNo);
        try {
            Refund refund = refundService.queryByOutRefundNo(request);
            return toResult(refund);
        } catch (Exception e) {
            log.error("微信退款查询失败 outRefundNo={}", outRefundNo, e);
            throw new ServiceException("微信退款查询失败: " + e.getMessage());
        }
    }

    private String resolveRefundNotifyUrl() {
        if (StringUtils.isNotBlank(wxPayProperties.getRefundNotifyUrl())) {
            return wxPayProperties.getRefundNotifyUrl();
        }
        log.warn("未配置 wxpay.refund-notify-url，退款结果仅依赖同步返回/主动查询");
        return null;
    }

    private RemoteWxRefundResult toResult(Refund refund) {
        RemoteWxRefundResult result = new RemoteWxRefundResult();
        result.setOutRefundNo(refund.getOutRefundNo());
        result.setRefundId(refund.getRefundId());
        result.setOutTradeNo(refund.getOutTradeNo());
        Status status = refund.getStatus();
        result.setStatus(status.name());
        return result;
    }
}
