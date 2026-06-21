package org.dromara.bidding.controller;

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
import org.dromara.bidding.domain.vo.RuleSettingVo;
import org.dromara.bidding.domain.bo.RuleSettingBo;
import org.dromara.bidding.service.IRuleSettingService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 竞价规则
 * 前端访问路由地址为:/bidding/ruleSetting
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/ruleSetting")
public class RuleSettingController extends BaseController {

    private final IRuleSettingService ruleSettingService;

    @SaCheckPermission("bidding:ruleSetting:list")
    @GetMapping("/list")
    public TableDataInfo<RuleSettingVo> list(RuleSettingBo bo, PageQuery pageQuery) {
        return ruleSettingService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:ruleSetting:export")
    @Log(title = "竞价规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(RuleSettingBo bo, HttpServletResponse response) {
        List<RuleSettingVo> list = ruleSettingService.queryList(bo);
        ExcelUtil.exportExcel(list, "竞价规则", RuleSettingVo.class, response);
    }

    @SaCheckPermission("bidding:ruleSetting:query")
    @GetMapping("/{id}")
    public R<RuleSettingVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(ruleSettingService.queryById(id));
    }

    @SaCheckPermission("bidding:ruleSetting:add")
    @Log(title = "竞价规则", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RuleSettingBo bo) {
        return toAjax(ruleSettingService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:ruleSetting:edit")
    @Log(title = "竞价规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody RuleSettingBo bo) {
        return toAjax(ruleSettingService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:ruleSetting:remove")
    @Log(title = "竞价规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(ruleSettingService.deleteWithValidByIds(List.of(ids), true));
    }
}
