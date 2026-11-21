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
import org.dromara.order.domain.bo.AfterSaleAuditBo;
import org.dromara.order.domain.bo.AfterSaleAuditCommand;
import org.dromara.order.domain.bo.AfterSaleAuditCommand.AuditorType;
import org.dromara.order.domain.bo.AfterSaleInspectBo;
import org.dromara.order.domain.bo.AfterSaleInspectCommand;
import org.dromara.order.domain.bo.AfterSaleInspectCommand.InspectorType;
import org.dromara.order.domain.bo.AfterSaleQueryBo;
import org.dromara.order.domain.bo.PendingShipForceRefundBo;
import org.dromara.order.domain.bo.PendingShipForceRefundCommand;
import org.dromara.order.domain.bo.PendingShipForceRefundCommand.OperatorType;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.vo.PcAfterSaleApplyResultVo;
import org.dromara.order.domain.vo.PcAfterSaleDetailVo;
import org.dromara.order.service.IAfterSaleAuditService;
import org.dromara.order.service.IAfterSaleInspectService;
import org.dromara.order.service.IPendingShipForceRefundService;
import org.dromara.order.util.LoginBizCodeHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/afterSale")
public class AfterSaleAuditController extends BaseController {

    private final IAfterSaleAuditService afterSaleAuditService;
    private final IAfterSaleInspectService afterSaleInspectService;
    private final IPendingShipForceRefundService pendingShipForceRefundService;
    private final LoginBizCodeHelper loginBizCodeHelper;

    /**
     * 商家售后列表
     */
    @SaCheckPermission("order:afterSale:merchantList")
    @GetMapping("/merchant/list")
    public TableDataInfo<AfterSaleVo> merchantList(AfterSaleQueryBo query,
                                                   @RequestParam @NotBlank(message = "商家编码不能为空") String memberCode,
                                                   PageQuery pageQuery) {
        if (query == null) {
            query = new AfterSaleQueryBo();
        }
        query.setMemberCode(memberCode);
        return afterSaleAuditService.queryPage(query, TenantHelper.getTenantId(), pageQuery);
    }

    /**
     * 商家售后详情
     */
    @SaCheckPermission("order:afterSale:merchantList")
    @GetMapping("/merchant/{afterSaleNo}")
    public R<PcAfterSaleDetailVo> merchantDetail(
        @PathVariable @NotBlank String afterSaleNo,
        @RequestParam @NotBlank(message = "商家编码不能为空") String memberCode) {
        return R.ok(afterSaleAuditService.detail(afterSaleNo, TenantHelper.getTenantId(), memberCode));
    }

    /**
     * 商家审核
     */
    @SaCheckPermission("order:afterSale:merchantAudit")
    @RepeatSubmit
    @Log(title = "商家审核售后", businessType = BusinessType.UPDATE)
    @PostMapping("/merchant/audit")
    public R<Void> merchantAudit(@Valid @RequestBody AfterSaleAuditBo bo) {
        afterSaleAuditService.audit(AfterSaleAuditCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .afterSaleNo(bo.getAfterSaleNo())
            .pass(bo.getPass())
            .auditRemark(bo.getAuditRemark())
            .returnAddressId(bo.getReturnAddressId())
            .auditorType(AuditorType.MERCHANT)
            .build());
        return R.ok();
    }

    /**
     * 运营售后列表
     */
    @SaCheckPermission("order:afterSale:opsList")
    @GetMapping("/ops/list")
    public TableDataInfo<AfterSaleVo> opsList(AfterSaleQueryBo query, PageQuery pageQuery) {
        return afterSaleAuditService.queryPage(query, TenantHelper.getTenantId(), pageQuery);
    }

    /**
     * 运营售后详情
     */
    @SaCheckPermission("order:afterSale:opsList")
    @GetMapping("/ops/{afterSaleNo}")
    public R<PcAfterSaleDetailVo> opsDetail(@PathVariable @NotBlank String afterSaleNo) {
        return R.ok(afterSaleAuditService.detail(afterSaleNo, TenantHelper.getTenantId(), null));
    }

    /**
     * 运营审核
     */
    @SaCheckPermission("order:afterSale:opsAudit")
    @RepeatSubmit
    @Log(title = "运营审核售后", businessType = BusinessType.UPDATE)
    @PostMapping("/ops/audit")
    public R<Void> opsAudit(@Valid @RequestBody AfterSaleAuditBo bo) {
        afterSaleAuditService.audit(AfterSaleAuditCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .afterSaleNo(bo.getAfterSaleNo())
            .pass(bo.getPass())
            .auditRemark(bo.getAuditRemark())
            .returnAddressId(bo.getReturnAddressId())
            .auditorType(AuditorType.OPS)
            .build());
        return R.ok();
    }

    /**
     * 商家待发货退款
     */
    @SaCheckPermission("order:afterSale:merchantAudit")
    @RepeatSubmit
    @Log(title = "商家待发货退款", businessType = BusinessType.UPDATE)
    @PostMapping("/merchant/refundPendingShip")
    public R<PcAfterSaleApplyResultVo> merchantRefundPendingShip(@Valid @RequestBody PendingShipForceRefundBo bo) {
        return R.ok(pendingShipForceRefundService.forceRefund(PendingShipForceRefundCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .orderNo(bo.getOrderNo())
            .memberCode(loginBizCodeHelper.requireUserinfoCode())
            .applyReason(bo.getApplyReason())
            .items(bo.getItems())
            .operatorType(OperatorType.MERCHANT)
            .build()));
    }

    /**
     * 运营待发货强制退款
     */
    @SaCheckPermission("order:afterSale:opsAudit")
    @RepeatSubmit
    @Log(title = "运营待发货强制退款", businessType = BusinessType.UPDATE)
    @PostMapping("/ops/refundPendingShip")
    public R<PcAfterSaleApplyResultVo> opsRefundPendingShip(@Valid @RequestBody PendingShipForceRefundBo bo) {
        return R.ok(pendingShipForceRefundService.forceRefund(PendingShipForceRefundCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .orderNo(bo.getOrderNo())
            .applyReason(bo.getApplyReason())
            .items(bo.getItems())
            .operatorType(OperatorType.OPS)
            .build()));
    }

    /**
     * 商家验货
     */
    @SaCheckPermission("order:afterSale:merchantInspect")
    @RepeatSubmit
    @Log(title = "商家验货售后", businessType = BusinessType.UPDATE)
    @PostMapping("/merchant/inspect")
    public R<Void> merchantInspect(@Valid @RequestBody AfterSaleInspectBo bo, @RequestParam @NotBlank(message = "商家编码不能为空") String memberCode) {
        afterSaleInspectService.inspect(AfterSaleInspectCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .afterSaleNo(bo.getAfterSaleNo())
            .pass(bo.getPass())
            .inspectRemark(bo.getInspectRemark())
            .inspectorType(InspectorType.MERCHANT)
            .memberCode(memberCode)
            .build());
        return R.ok();
    }

    /**
     * 运营验货
     */
    @SaCheckPermission("order:afterSale:opsInspect")
    @RepeatSubmit
    @Log(title = "运营验货售后", businessType = BusinessType.UPDATE)
    @PostMapping("/ops/inspect")
    public R<Void> opsInspect(@Valid @RequestBody AfterSaleInspectBo bo) {
        afterSaleInspectService.inspect(AfterSaleInspectCommand.builder()
            .tenantId(TenantHelper.getTenantId())
            .afterSaleNo(bo.getAfterSaleNo())
            .pass(bo.getPass())
            .inspectRemark(bo.getInspectRemark())
            .inspectorType(InspectorType.OPS)
            .build());
        return R.ok();
    }
}
