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
import org.dromara.bidding.domain.vo.FinancialApplyVo;
import org.dromara.bidding.domain.bo.FinancialApplyBo;
import org.dromara.bidding.service.IFinancialApplyService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 清算申请
 * 前端访问路由地址为:/bidding/financialApply
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/financialApply")
public class FinancialApplyController extends BaseController {

    private final IFinancialApplyService financialApplyService;

    @SaCheckPermission("bidding:financialApply:list")
    @GetMapping("/list")
    public TableDataInfo<FinancialApplyVo> list(FinancialApplyBo bo, PageQuery pageQuery) {
        return financialApplyService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:financialApply:export")
    @Log(title = "清算申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(FinancialApplyBo bo, HttpServletResponse response) {
        List<FinancialApplyVo> list = financialApplyService.queryList(bo);
        ExcelUtil.exportExcel(list, "清算申请", FinancialApplyVo.class, response);
    }

    @SaCheckPermission("bidding:financialApply:query")
    @GetMapping("/{id}")
    public R<FinancialApplyVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(financialApplyService.queryById(id));
    }

    @SaCheckPermission("bidding:financialApply:add")
    @Log(title = "清算申请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody FinancialApplyBo bo) {
        return toAjax(financialApplyService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:financialApply:edit")
    @Log(title = "清算申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody FinancialApplyBo bo) {
        return toAjax(financialApplyService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:financialApply:remove")
    @Log(title = "清算申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(financialApplyService.deleteWithValidByIds(List.of(ids), true));
    }
}
