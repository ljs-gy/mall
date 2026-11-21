package org.dromara.pay.controller;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.pay.domain.bo.WxJsapiPrepayBo;
import org.dromara.pay.domain.bo.WxNativePrepayBo;
import org.dromara.pay.domain.vo.WxNativePrepayVo;
import org.dromara.pay.service.IWxPayPrepayService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/wx")
public class WxPayController extends BaseController {

    private final IWxPayPrepayService wxPayPrepayService;


    @PostMapping("/native/prepay")
    public R<WxNativePrepayVo> nativePrepay(@Valid @RequestBody WxNativePrepayBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        WxNativePrepayVo vo = wxPayPrepayService.createNativePrepay(bo, TenantHelper.getTenantId(), LoginHelper.getUserId());
        return R.ok(vo);
    }


    @PostMapping("/jsapi/prepay")
    public R<PrepayWithRequestPaymentResponse> jsapiPrepay(@Valid @RequestBody WxJsapiPrepayBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        return R.ok(wxPayPrepayService.createJsapiPrepay(
            bo, TenantHelper.getTenantId(), LoginHelper.getUserId()));
    }
}
