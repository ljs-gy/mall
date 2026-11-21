package org.dromara.pm.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
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
import org.dromara.pm.domain.vo.CouponGiftGrantRuleVo;
import org.dromara.pm.domain.bo.CouponGiftGrantRuleAuditBo;
import org.dromara.pm.domain.bo.CouponGiftGrantRuleBo;
import org.dromara.pm.service.ICouponGiftGrantRuleService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 满赠券发放规则
 * 前端访问路由地址为:/pm/couponGiftGrantRule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/couponGiftGrantRule")
public class CouponGiftGrantRuleController extends BaseController {

    private final ICouponGiftGrantRuleService couponGiftGrantRuleService;

    /**
     * 查询满赠券发放规则列表
     */
    @SaCheckPermission("pm:couponGiftGrantRule:list")
    @GetMapping("/list")
    public TableDataInfo<CouponGiftGrantRuleVo> list(CouponGiftGrantRuleBo bo, PageQuery pageQuery) {
        return couponGiftGrantRuleService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出满赠券发放规则列表
     */
    @SaCheckPermission("pm:couponGiftGrantRule:export")
    @Log(title = "满赠券发放规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CouponGiftGrantRuleBo bo, HttpServletResponse response) {
        List<CouponGiftGrantRuleVo> list = couponGiftGrantRuleService.queryList(bo);
        ExcelUtil.exportExcel(list, "满赠券发放规则", CouponGiftGrantRuleVo.class, response);
    }

    /**
     * 获取满赠券发放规则详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:couponGiftGrantRule:query")
    @GetMapping("/{id}")
    public R<CouponGiftGrantRuleVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(couponGiftGrantRuleService.queryById(id));
    }

    /**
     * 新增满赠券发放规则
     */
    @SaCheckPermission("pm:couponGiftGrantRule:add")
    @Log(title = "满赠券发放规则", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CouponGiftGrantRuleBo bo) {
        return toAjax(couponGiftGrantRuleService.insertByBo(bo));
    }

    /**
     * 审核满赠券发放规则
     */
    @SaCheckPermission("pm:couponGiftGrantRule:audit")
    @Log(title = "满赠券发放规则审核", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody CouponGiftGrantRuleAuditBo bo) {
        couponGiftGrantRuleService.auditGiftGrantRule(bo);
        return R.ok();
    }

    /**
     * 修改满赠券发放规则
     */
    @SaCheckPermission("pm:couponGiftGrantRule:edit")
    @Log(title = "满赠券发放规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CouponGiftGrantRuleBo bo) {
        return toAjax(couponGiftGrantRuleService.updateByBo(bo));
    }

    /**
     * 删除满赠券发放规则
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:couponGiftGrantRule:remove")
    @Log(title = "满赠券发放规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(couponGiftGrantRuleService.deleteWithValidByIds(List.of(ids), true));
    }
}
