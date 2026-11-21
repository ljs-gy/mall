package org.dromara.user.controller;

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
import org.dromara.user.domain.vo.DepartmentVo;
import org.dromara.user.domain.bo.DepartmentBo;
import org.dromara.user.service.IDepartmentService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 部门
 * 前端访问路由地址为:/user/department
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/department")
public class DepartmentController extends BaseController {

    private final IDepartmentService departmentService;

    /**
     * 查询部门列表
     */
    @SaCheckPermission("user:department:list")
    @GetMapping("/list")
    public TableDataInfo<DepartmentVo> list(DepartmentBo bo, PageQuery pageQuery) {
        return departmentService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出部门列表
     */
    @SaCheckPermission("user:department:export")
    @Log(title = "部门", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DepartmentBo bo, HttpServletResponse response) {
        List<DepartmentVo> list = departmentService.queryList(bo);
        ExcelUtil.exportExcel(list, "部门", DepartmentVo.class, response);
    }

    /**
     * 获取部门详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:department:query")
    @GetMapping("/{id}")
    public R<DepartmentVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(departmentService.queryById(id));
    }

    /**
     * 新增部门
     */
    @SaCheckPermission("user:department:add")
    @Log(title = "部门", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DepartmentBo bo) {
        return toAjax(departmentService.insertByBo(bo));
    }

    /**
     * 修改部门
     */
    @SaCheckPermission("user:department:edit")
    @Log(title = "部门", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartmentBo bo) {
        return toAjax(departmentService.updateByBo(bo));
    }

    /**
     * 删除部门
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:department:remove")
    @Log(title = "部门", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(departmentService.deleteWithValidByIds(List.of(ids), true));
    }
}
