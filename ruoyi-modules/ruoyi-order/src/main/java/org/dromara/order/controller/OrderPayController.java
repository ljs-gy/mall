package org.dromara.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.order.domain.bo.ZeroPayConfirmBo;
import org.dromara.order.service.IOrderPayService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收银台支付确认
 * 网关路径：/order/pay/**
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/pay")
public class OrderPayController extends BaseController {

    private final IOrderPayService orderPayService;

    /**
     * 0 元订单确认支付（收银台「立即支付」金额为 0 时调用）
     */
    @PostMapping("/zeroConfirm")
    public R<Void> zeroConfirm(@Valid @RequestBody ZeroPayConfirmBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        orderPayService.confirmZeroPay(
            TenantHelper.getTenantId(), LoginHelper.getUserId(), bo.getOrderNo());
        return R.ok();
    }
}
