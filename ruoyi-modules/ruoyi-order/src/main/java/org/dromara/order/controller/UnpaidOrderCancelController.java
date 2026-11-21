package org.dromara.order.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.order.domain.bo.CancelUnpaidCommand;
import org.dromara.order.domain.bo.CancelUnpaidCommand.UnpaidCancelOperatorType;
import org.dromara.order.domain.bo.UnpaidOrderCancelBo;
import org.dromara.order.service.IOrderUnpaidCancelService;
import org.dromara.order.util.LoginBizCodeHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/unpaid")
public class UnpaidOrderCancelController extends BaseController {

    private final IOrderUnpaidCancelService orderUnpaidCancelService;
    private final LoginBizCodeHelper loginBizCodeHelper;

    /**
     * 买家取消未支付子单
     */
    @RepeatSubmit
    @PostMapping("/pc/cancel")
    public R<Void> buyerCancel(@Valid @RequestBody UnpaidOrderCancelBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        orderUnpaidCancelService.cancelUnpaid(CancelUnpaidCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .orderNo(bo.getOrderNo())
            .operatorType(UnpaidCancelOperatorType.BUYER)
            .operatorId(loginBizCodeHelper.requireUserinfoCode())
            .build());
        return R.ok();
    }

    /**
     * 商家取消本店未支付子单
     */
    @SaCheckPermission("order:unpaid:merchantCancel")
    @RepeatSubmit
    @Log(title = "商家取消未支付订单", businessType = BusinessType.UPDATE)
    @PostMapping("/merchant/cancel")
    public R<Void> merchantCancel(@Valid @RequestBody UnpaidOrderCancelBo bo) {
        orderUnpaidCancelService.cancelUnpaid(CancelUnpaidCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .orderNo(bo.getOrderNo())
            .operatorType(UnpaidCancelOperatorType.MERCHANT)
            .operatorId(loginBizCodeHelper.requireUserinfoCode())
            .build());
        return R.ok();
    }

    /**
     * 运营取消未支付子单
     */
    @SaCheckPermission("order:unpaid:opsCancel")
    @RepeatSubmit
    @Log(title = "运营取消未支付订单", businessType = BusinessType.UPDATE)
    @PostMapping("/ops/cancel")
    public R<Void> opsCancel(@Valid @RequestBody UnpaidOrderCancelBo bo) {
        orderUnpaidCancelService.cancelUnpaid(CancelUnpaidCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .orderNo(bo.getOrderNo())
            .operatorType(UnpaidCancelOperatorType.OPS)
            .build());
        return R.ok();
    }
}
