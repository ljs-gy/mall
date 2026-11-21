package org.dromara.order.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.order.domain.bo.MerchantShippingQueryBo;
import org.dromara.order.domain.bo.MerchantShippingShipBo;
import org.dromara.order.domain.vo.MerchantShippingDetailVo;
import org.dromara.order.domain.vo.ShippingOrderVo;
import org.dromara.order.service.IMerchantShippingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家发货
 * 网关：/order/shipping/**
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/shipping")
public class MerchantShippingController extends BaseController {

    private final IMerchantShippingService merchantShippingService;

    /**
     * 商家发货单列表（须传本店 memberCode）
     */
    @SaCheckPermission("order:shipping:merchantList")
    @GetMapping("/merchant/list")
    public TableDataInfo<ShippingOrderVo> merchantList(MerchantShippingQueryBo query,
                                                       @RequestParam @NotBlank(message = "商家编码不能为空") String memberCode,
                                                       PageQuery pageQuery) {
        if (query == null) {
            query = new MerchantShippingQueryBo();
        }
        query.setMemberCode(memberCode);
        return merchantShippingService.queryPage(query, TenantHelper.getTenantId(), pageQuery);
    }

    /**
     * 商家发货单详情
     */
    @SaCheckPermission("order:shipping:merchantList")
    @GetMapping("/merchant/{shippingNo}")
    public R<MerchantShippingDetailVo> merchantDetail(
        @PathVariable @NotBlank String shippingNo,
        @RequestParam @NotBlank(message = "商家编码不能为空") String memberCode) {
        return R.ok(merchantShippingService.detail(shippingNo, TenantHelper.getTenantId(), memberCode));
    }

    /**
     * 商家确认发货（发货单 10→20，订单 20→30）
     */
    @SaCheckPermission("order:shipping:merchantShip")
    @RepeatSubmit
    @Log(title = "商家发货", businessType = BusinessType.UPDATE)
    @PostMapping("/merchant/ship")
    public R<Void> merchantShip(@Valid @RequestBody MerchantShippingShipBo bo,
                                @RequestParam @NotBlank(message = "商家编码不能为空") String memberCode) {
        merchantShippingService.ship(bo, TenantHelper.getTenantId(), memberCode);
        return R.ok();
    }
}
