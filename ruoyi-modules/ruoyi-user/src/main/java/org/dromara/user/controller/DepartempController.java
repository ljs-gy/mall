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
import org.dromara.user.domain.vo.DepartempVo;
import org.dromara.user.domain.bo.DepartempBo;
import org.dromara.user.service.IDepartempService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 部门员工职位关系
 * 前端访问路由地址为:/user/departemp
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/departemp")
public class DepartempController extends BaseController {

    private final IDepartempService departempService;

    /**
     * 查询部门员工职位关系列表
     */
    @SaCheckPermission("user:departemp:list")
    @GetMapping("/list")
    public TableDataInfo<DepartempVo> list(DepartempBo bo, PageQuery pageQuery) {
        return departempService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出部门员工职位关系列表
     */
    @SaCheckPermission("user:departemp:export")
    @Log(title = "部门员工职位关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DepartempBo bo, HttpServletResponse response) {
        List<DepartempVo> list = departempService.queryList(bo);
        ExcelUtil.exportExcel(list, "部门员工职位关系", DepartempVo.class, response);
    }

    /**
     * 获取部门员工职位关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:departemp:query")
    @GetMapping("/{id}")
    public R<DepartempVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(departempService.queryById(id));
    }

    /**
     * 新增部门员工职位关系
     */
    @SaCheckPermission("user:departemp:add")
    @Log(title = "部门员工职位关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DepartempBo bo) {
        return toAjax(departempService.insertByBo(bo));
    }

    /**
     * 修改部门员工职位关系
     */
    @SaCheckPermission("user:departemp:edit")
    @Log(title = "部门员工职位关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DepartempBo bo) {
        return toAjax(departempService.updateByBo(bo));
    }

    /**
     * 删除部门员工职位关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:departemp:remove")
    @Log(title = "部门员工职位关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(departempService.deleteWithValidByIds(List.of(ids), true));
    }
}
