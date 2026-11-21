package org.dromara.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.order.domain.bo.PcBuyNowOrderSubmitBo;
import org.dromara.order.domain.bo.PcCartOrderSubmitBo;
import org.dromara.order.domain.bo.PcCheckoutConfirmBo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;
import org.dromara.order.domain.vo.PcOrderSubmitResultVo;
import org.dromara.order.service.IPcCheckoutService;
import org.dromara.order.service.IPcOrderSubmitService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PC 商城结算确认页 / 提交订单
 * 网关路径：/order/mall/checkout/**
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/checkout")
public class PcCheckoutController extends BaseController {

    private final IPcCheckoutService pcCheckoutService;
    private final IPcOrderSubmitService pcOrderSubmitService;

    /**
     * 结算确认页算价
     */
    @PostMapping("/confirm")
    public R<PcCheckoutConfirmVo> confirm(@Valid @RequestBody PcCheckoutConfirmBo bo) {
        return R.ok(pcCheckoutService.confirmCheckout(bo));
    }

    /**
     * 购物车提交订单
     */
    @PostMapping("/submit/from-cart")
    public R<PcOrderSubmitResultVo> submitFromCart(@Valid @RequestBody PcCartOrderSubmitBo bo) {
        return R.ok(pcOrderSubmitService.submitFromCart(bo));
    }

    /**
     * 立即购买提交订单
     */
    @PostMapping("/submit/buy-now")
    public R<PcOrderSubmitResultVo> submitBuyNow(@Valid @RequestBody PcBuyNowOrderSubmitBo bo) {
        return R.ok(pcOrderSubmitService.submitBuyNow(bo));
    }
}
