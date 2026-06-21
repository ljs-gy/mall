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
import org.dromara.bidding.domain.vo.ApplyRecordVo;
import org.dromara.bidding.domain.bo.ApplyRecordBo;
import org.dromara.bidding.service.IApplyRecordService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 报名记录
 * 前端访问路由地址为:/bidding/applyRecord
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/applyRecord")
public class ApplyRecordController extends BaseController {

    private final IApplyRecordService applyRecordService;

    @SaCheckPermission("bidding:applyRecord:list")
    @GetMapping("/list")
    public TableDataInfo<ApplyRecordVo> list(ApplyRecordBo bo, PageQuery pageQuery) {
        return applyRecordService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:applyRecord:export")
    @Log(title = "报名记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ApplyRecordBo bo, HttpServletResponse response) {
        List<ApplyRecordVo> list = applyRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "报名记录", ApplyRecordVo.class, response);
    }

    @SaCheckPermission("bidding:applyRecord:query")
    @GetMapping("/{id}")
    public R<ApplyRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(applyRecordService.queryById(id));
    }

    @SaCheckPermission("bidding:applyRecord:add")
    @Log(title = "报名记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ApplyRecordBo bo) {
        return toAjax(applyRecordService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:applyRecord:edit")
    @Log(title = "报名记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ApplyRecordBo bo) {
        return toAjax(applyRecordService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:applyRecord:remove")
    @Log(title = "报名记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(applyRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
