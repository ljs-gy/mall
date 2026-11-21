package org.dromara.pm.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.pm.domain.vo.CouponTemplateVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;

import org.dromara.pm.domain.bo.CouponTemplateAuditBo;
import org.dromara.pm.domain.bo.CouponTemplateBo;
import org.dromara.pm.service.ICouponTemplateService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 优惠券
 * 前端访问路由地址为:/pm/couponTemplate
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/couponTemplate")
public class CouponTemplateController extends BaseController {

    private final ICouponTemplateService couponTemplateService;

    /**
     * 查询优惠券列表
     */
    @SaCheckPermission("pm:couponTemplate:list")
    @GetMapping("/list")
    public TableDataInfo<CouponTemplateVo> list(CouponTemplateBo bo, PageQuery pageQuery) {
        return couponTemplateService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出优惠券列表
     */
    @SaCheckPermission("pm:couponTemplate:export")
    @Log(title = "优惠券", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CouponTemplateBo bo, HttpServletResponse response) {
        List<CouponTemplateVo> list = couponTemplateService.queryList(bo);
        ExcelUtil.exportExcel(list, "优惠券", CouponTemplateVo.class, response);
    }

    /**
     * 获取优惠券详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:couponTemplate:query")
    @GetMapping("/{id}")
    public R<CouponTemplateVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(couponTemplateService.queryById(id));
    }

    /**
     * 新增优惠券
     */
    @SaCheckPermission("pm:couponTemplate:add")
    @Log(title = "优惠券", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CouponTemplateBo bo) {
        return toAjax(couponTemplateService.insertByBo(bo));
    }

    /**
     * 审核优惠券
     */
    @SaCheckPermission("pm:couponTemplate:audit")
    @Log(title = "优惠券审核", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody CouponTemplateAuditBo bo) {
        couponTemplateService.auditCouponTemplate(bo);
        return R.ok();
    }

    /**
     * 修改优惠券
     */
    @SaCheckPermission("pm:couponTemplate:edit")
    @Log(title = "优惠券", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CouponTemplateBo bo) {
        return toAjax(couponTemplateService.updateByBo(bo));
    }

    /**
     * 删除优惠券
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:couponTemplate:remove")
    @Log(title = "优惠券", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(couponTemplateService.deleteWithValidByIds(List.of(ids), true));
    }
}
