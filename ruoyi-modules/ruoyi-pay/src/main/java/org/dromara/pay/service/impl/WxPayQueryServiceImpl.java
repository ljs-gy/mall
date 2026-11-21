package org.dromara.pay.service.impl;

import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.pay.api.domain.RemoteWxPayStatus;
import org.dromara.pay.config.WeChatPayProperties;
import org.dromara.pay.service.IWxPayQueryService;
import org.springframework.stereotype.Service;

/**
 * 微信支付查单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayQueryServiceImpl implements IWxPayQueryService {

    private static final String ERR_ORDER_NOT_EXIST = "ORDER_NOT_EXIST";

    private final WeChatPayProperties wxPayProperties;
    private final NativePayService nativePayService;

    /**
     * 微信支付主动查单
     */
    @Override
    public RemoteWxPayStatus queryByOutTradeNo(String parentOrderNo) {
        if (StringUtils.isBlank(parentOrderNo)) {
            return null;
        }
        String mchid = wxPayProperties.getMerchantId();
        if (StringUtils.isBlank(mchid)) {
            log.warn("微信商户号未配置，无法查单 parentOrderNo={}", parentOrderNo);
            return null;
        }

        QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
        queryRequest.setMchid(mchid);
        queryRequest.setOutTradeNo(parentOrderNo);
        try {
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(queryRequest);
            return toRemoteStatus(transaction);
        } catch (ServiceException e) {
            if (ERR_ORDER_NOT_EXIST.equals(e.getErrorCode())) {
                RemoteWxPayStatus status = new RemoteWxPayStatus();
                status.setTradeState(RemoteWxPayStatus.NOT_EXIST);
                return status;
            }
            log.error("查询微信订单失败 parentOrderNo={} code={} msg={}", parentOrderNo, e.getErrorCode(), e.getErrorMessage());
            return null;
        } catch (HttpException e) {
            log.error("查询微信订单网络异常 parentOrderNo={}", parentOrderNo, e);
            return null;
        }
    }

    private RemoteWxPayStatus toRemoteStatus(Transaction transaction) {
        RemoteWxPayStatus status = new RemoteWxPayStatus();
        status.setTradeState(transaction.getTradeState().name());
        status.setTransactionId(transaction.getTransactionId());
        status.setAttach(transaction.getAttach());
        if (transaction.getAmount() != null) {
            status.setPayAmountCent(transaction.getAmount().getTotal());
        }
        return status;
    }
}
