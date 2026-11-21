package org.dromara.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.order.domain.bo.PcOrderReceiveBo;
import org.dromara.order.service.IOrderReceiveService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/pc")
public class PcOrderReceiveController extends BaseController {

    private final IOrderReceiveService orderReceiveService;

    /**
     * 确认收货
     */
    @RepeatSubmit
    @PostMapping("/receive")
    public R<Void> receive(@Valid @RequestBody PcOrderReceiveBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        orderReceiveService.receiveByBuyer(TenantHelper.getTenantId(), bo.getOrderNo(), LoginHelper.getUserId());
        return R.ok();
    }
}
