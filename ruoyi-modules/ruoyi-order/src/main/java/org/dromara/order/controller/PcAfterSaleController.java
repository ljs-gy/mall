package org.dromara.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.order.domain.bo.PcAfterSaleApplyBo;
import org.dromara.order.domain.bo.PcAfterSaleReturnExpressBo;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.vo.PcAfterSaleApplyResultVo;
import org.dromara.order.domain.vo.PcAfterSaleDetailVo;
import org.dromara.order.service.IPcAfterSaleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/afterSale/pc")
public class PcAfterSaleController extends BaseController {

    private final IPcAfterSaleService pcAfterSaleService;

    /**
     * 申请售后
     */
    @RepeatSubmit
    @PostMapping("/apply")
    public R<PcAfterSaleApplyResultVo> apply(@Valid @RequestBody PcAfterSaleApplyBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        return R.ok(pcAfterSaleService.apply(bo, TenantHelper.getTenantId(), LoginHelper.getUserId()));
    }

    /**
     * 我的售后列表
     */
    @GetMapping("/list")
    public TableDataInfo<AfterSaleVo> list(PageQuery pageQuery) {
        if (!LoginHelper.isLogin()) {
            return TableDataInfo.build();
        }
        return pcAfterSaleService.listMine(TenantHelper.getTenantId(), LoginHelper.getUserId(), pageQuery);
    }

    /**
     * 撤销售后
     */
    @RepeatSubmit
    @PostMapping("/revoke/{afterSaleNo}")
    public R<Void> revoke(@NotBlank(message = "售后单号不能为空") @PathVariable String afterSaleNo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        pcAfterSaleService.revoke(afterSaleNo, TenantHelper.getTenantId(), LoginHelper.getUserId());
        return R.ok();
    }

    /**
     * 填写退货快递
     */
    @RepeatSubmit
    @PostMapping("/returnExpress")
    public R<Void> returnExpress(@Valid @RequestBody PcAfterSaleReturnExpressBo bo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        pcAfterSaleService.fillReturnExpress(bo, TenantHelper.getTenantId(), LoginHelper.getUserId());
        return R.ok();
    }

    /**
     * 售后详情
     */
    @GetMapping("/{afterSaleNo}")
    public R<PcAfterSaleDetailVo> detail(@NotBlank(message = "售后单号不能为空") @PathVariable String afterSaleNo) {
        if (!LoginHelper.isLogin()) {
            return R.fail("请先登录");
        }
        return R.ok(pcAfterSaleService.detail(afterSaleNo, TenantHelper.getTenantId(), LoginHelper.getUserId()));
    }
}
