package org.dromara.pay.service.impl;

import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.pay.api.domain.RemoteWxPayStatus;
import org.dromara.pay.config.WeChatPayProperties;
import org.dromara.pay.service.IWxPayCloseService;
import org.dromara.pay.service.IWxPayQueryService;
import org.springframework.stereotype.Service;

/**
 * 微信关单：先查单再关单，仅处理 NOTPAY / USERPAYING。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayCloseServiceImpl implements IWxPayCloseService {

    private static final String ERR_ORDER_NOT_EXIST = "ORDER_NOT_EXIST";
    private static final String ERR_ORDER_CLOSED = "ORDER_CLOSED";

    private final WeChatPayProperties wxPayProperties;
    private final NativePayService nativePayService;
    private final IWxPayQueryService wxPayQueryService;

    @Override
    public void closeUnpaidOrder(String parentOrderNo) {
        if (StringUtils.isBlank(parentOrderNo)) {
            return;
        }
        String mchid = wxPayProperties.getMerchantId();
        if (StringUtils.isBlank(mchid)) {
            log.warn("微信商户号未配置，跳过关单 parentOrderNo={}", parentOrderNo);
            return;
        }

        RemoteWxPayStatus status = wxPayQueryService.queryByOutTradeNo(parentOrderNo);
        if (status == null) {
            log.warn("查询微信订单失败，跳过关单 parentOrderNo={}", parentOrderNo);
            return;
        }
        if (RemoteWxPayStatus.NOT_EXIST.equals(status.getTradeState())) {
            log.info("微信侧无支付单，无需关单 parentOrderNo={}", parentOrderNo);
            return;
        }

        String tradeState = status.getTradeState();
        if (RemoteWxPayStatus.SUCCESS.equals(tradeState)) {
            log.warn("微信订单已支付，跳过关单 parentOrderNo={} transactionId={}", parentOrderNo, status.getTransactionId());
            return;
        }
        if (RemoteWxPayStatus.CLOSED.equals(tradeState) || RemoteWxPayStatus.REVOKED.equals(tradeState)) {
            log.info("微信订单已关闭，跳过 parentOrderNo={} tradeState={}", parentOrderNo, tradeState);
            return;
        }
        if (RemoteWxPayStatus.NOTPAY.equals(tradeState) || RemoteWxPayStatus.USERPAYING.equals(tradeState)) {
            invokeCloseOrder(parentOrderNo, mchid);
            return;
        }
        log.info("微信订单状态无需关单 parentOrderNo={} tradeState={}", parentOrderNo, tradeState);
    }

    private void invokeCloseOrder(String parentOrderNo, String mchid) {
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(mchid);
        closeRequest.setOutTradeNo(parentOrderNo);
        try {
            nativePayService.closeOrder(closeRequest);
            log.info("微信关单成功 parentOrderNo={}", parentOrderNo);
        } catch (ServiceException e) {
            if (isIgnorableWxError(e)) {
                log.info("微信关单幂等跳过 parentOrderNo={} code={}", parentOrderNo, e.getErrorCode());
                return;
            }
            log.error("微信关单失败 parentOrderNo={} code={} msg={}", parentOrderNo, e.getErrorCode(), e.getErrorMessage());
        } catch (HttpException e) {
            log.error("微信关单网络异常 parentOrderNo={}", parentOrderNo, e);
        }
    }

    private boolean isIgnorableWxError(ServiceException e) {
        String code = e.getErrorCode();
        return ERR_ORDER_NOT_EXIST.equals(code) || ERR_ORDER_CLOSED.equals(code);
    }
}
