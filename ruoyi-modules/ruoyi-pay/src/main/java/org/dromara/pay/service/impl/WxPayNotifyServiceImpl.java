package org.dromara.pay.service.impl;

import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.RemoteOrderPayConfirmBo;
import org.dromara.pay.service.IWxPayNotifyService;
import org.springframework.stereotype.Service;

import java.util.Date;


@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayNotifyServiceImpl implements IWxPayNotifyService {

    @DubboReference
    private RemoteOrderService remoteOrderService;

    /**
     * 微信支付成功回调：验签后把微信结果落到订单「已支付」。
     */
    @Override
    public boolean processPaymentSuccess(Transaction transaction) {
        String outTradeNo = transaction.getOutTradeNo();
        String tradeState = transaction.getTradeState().name();
        if (!"SUCCESS".equals(tradeState)) {
            log.warn("支付单 [{}] 交易状态非成功: {}", outTradeNo, tradeState);
            return true;
        }
        RemoteOrderPayConfirmBo confirmBo = new RemoteOrderPayConfirmBo();
        confirmBo.setTenantId(transaction.getAttach());
        confirmBo.setThirdTradeNo(transaction.getTransactionId());
        confirmBo.setPayAmountCent(transaction.getAmount().getTotal());
        confirmBo.setPayTime(new Date());
        boolean success;
        if (isParentOutTradeNo(outTradeNo)) {
            confirmBo.setParentOrderNo(outTradeNo);
            success = remoteOrderService.confirmParentOrderPaid(confirmBo);
        } else {
            confirmBo.setOrderNo(outTradeNo);
            success = remoteOrderService.confirmOrderPaid(confirmBo);
        }
        if (!success) {
            log.error("订单支付确认失败，将响应 FAIL 供微信重试 outTradeNo={}", outTradeNo);
        }
        return success;
    }

    private boolean isParentOutTradeNo(String outTradeNo) {
        return StringUtils.isNotBlank(outTradeNo) && outTradeNo.startsWith("P");
    }
}
