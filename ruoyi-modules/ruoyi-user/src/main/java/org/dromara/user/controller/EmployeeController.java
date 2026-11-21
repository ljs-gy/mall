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
import org.dromara.user.domain.vo.EmployeeVo;
import org.dromara.user.domain.bo.EmployeeBo;
import org.dromara.user.service.IEmployeeService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 员工
 * 前端访问路由地址为:/user/employee
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/employee")
public class EmployeeController extends BaseController {

    private final IEmployeeService employeeService;

    /**
     * 查询员工列表
     */
    @SaCheckPermission("user:employee:list")
    @GetMapping("/list")
    public TableDataInfo<EmployeeVo> list(EmployeeBo bo, PageQuery pageQuery) {
        return employeeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出员工列表
     */
    @SaCheckPermission("user:employee:export")
    @Log(title = "员工", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(EmployeeBo bo, HttpServletResponse response) {
        List<EmployeeVo> list = employeeService.queryList(bo);
        ExcelUtil.exportExcel(list, "员工", EmployeeVo.class, response);
    }

    /**
     * 获取员工详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:employee:query")
    @GetMapping("/{id}")
    public R<EmployeeVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(employeeService.queryById(id));
    }

    /**
     * 新增员工
     */
    @SaCheckPermission("user:employee:add")
    @Log(title = "员工", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody EmployeeBo bo) {
        return toAjax(employeeService.insertByBo(bo));
    }

    /**
     * 修改员工
     */
    @SaCheckPermission("user:employee:edit")
    @Log(title = "员工", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody EmployeeBo bo) {
        return toAjax(employeeService.updateByBo(bo));
    }

    /**
     * 删除员工
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:employee:remove")
    @Log(title = "员工", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(employeeService.deleteWithValidByIds(List.of(ids), true));
    }
}
