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
import org.dromara.pm.domain.vo.PromoTierRuleVo;
import org.dromara.pm.domain.bo.PromoTierRuleBo;
import org.dromara.pm.service.IPromoTierRuleService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 营销活动阶梯规则
 * 前端访问路由地址为:/pm/promoTierRule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promoTierRule")
public class PromoTierRuleController extends BaseController {

    private final IPromoTierRuleService promoTierRuleService;

    /**
     * 查询营销活动阶梯规则列表
     */
    @SaCheckPermission("pm:promoTierRule:list")
    @GetMapping("/list")
    public TableDataInfo<PromoTierRuleVo> list(PromoTierRuleBo bo, PageQuery pageQuery) {
        return promoTierRuleService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出营销活动阶梯规则列表
     */
    @SaCheckPermission("pm:promoTierRule:export")
    @Log(title = "营销活动阶梯规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PromoTierRuleBo bo, HttpServletResponse response) {
        List<PromoTierRuleVo> list = promoTierRuleService.queryList(bo);
        ExcelUtil.exportExcel(list, "营销活动阶梯规则", PromoTierRuleVo.class, response);
    }

    /**
     * 获取营销活动阶梯规则详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:promoTierRule:query")
    @GetMapping("/{id}")
    public R<PromoTierRuleVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(promoTierRuleService.queryById(id));
    }

    /**
     * 新增营销活动阶梯规则
     */
    @SaCheckPermission("pm:promoTierRule:add")
    @Log(title = "营销活动阶梯规则", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PromoTierRuleBo bo) {
        return toAjax(promoTierRuleService.insertByBo(bo));
    }

    /**
     * 修改营销活动阶梯规则
     */
    @SaCheckPermission("pm:promoTierRule:edit")
    @Log(title = "营销活动阶梯规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PromoTierRuleBo bo) {
        return toAjax(promoTierRuleService.updateByBo(bo));
    }

    /**
     * 删除营销活动阶梯规则
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:promoTierRule:remove")
    @Log(title = "营销活动阶梯规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(promoTierRuleService.deleteWithValidByIds(List.of(ids), true));
    }
}
