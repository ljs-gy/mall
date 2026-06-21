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
import org.dromara.bidding.domain.vo.ApplyItemRelVo;
import org.dromara.bidding.domain.bo.ApplyItemRelBo;
import org.dromara.bidding.service.IApplyItemRelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 报名标的关联
 * 前端访问路由地址为:/bidding/applyItemRel
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/applyItemRel")
public class ApplyItemRelController extends BaseController {

    private final IApplyItemRelService applyItemRelService;

    @SaCheckPermission("bidding:applyItemRel:list")
    @GetMapping("/list")
    public TableDataInfo<ApplyItemRelVo> list(ApplyItemRelBo bo, PageQuery pageQuery) {
        return applyItemRelService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:applyItemRel:export")
    @Log(title = "报名标的关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ApplyItemRelBo bo, HttpServletResponse response) {
        List<ApplyItemRelVo> list = applyItemRelService.queryList(bo);
        ExcelUtil.exportExcel(list, "报名标的关联", ApplyItemRelVo.class, response);
    }

    @SaCheckPermission("bidding:applyItemRel:query")
    @GetMapping("/{id}")
    public R<ApplyItemRelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(applyItemRelService.queryById(id));
    }

    @SaCheckPermission("bidding:applyItemRel:add")
    @Log(title = "报名标的关联", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ApplyItemRelBo bo) {
        return toAjax(applyItemRelService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:applyItemRel:edit")
    @Log(title = "报名标的关联", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ApplyItemRelBo bo) {
        return toAjax(applyItemRelService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:applyItemRel:remove")
    @Log(title = "报名标的关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(applyItemRelService.deleteWithValidByIds(List.of(ids), true));
    }
}
