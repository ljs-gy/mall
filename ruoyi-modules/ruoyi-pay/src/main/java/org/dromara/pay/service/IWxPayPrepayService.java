package org.dromara.pay.service;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import org.dromara.pay.domain.bo.WxJsapiPrepayBo;
import org.dromara.pay.domain.bo.WxNativePrepayBo;
import org.dromara.pay.domain.vo.WxNativePrepayVo;

/**
 * 微信预下单
 */
public interface IWxPayPrepayService {

    WxNativePrepayVo createNativePrepay(WxNativePrepayBo bo, String tenantId, Long userinfoId);

    PrepayWithRequestPaymentResponse createJsapiPrepay(WxJsapiPrepayBo bo, String tenantId, Long userinfoId);
}
