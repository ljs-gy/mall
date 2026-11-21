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
import org.dromara.user.domain.vo.OrganizationVo;
import org.dromara.user.domain.bo.OrganizationBo;
import org.dromara.user.service.IOrganizationService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 组织
 * 前端访问路由地址为:/user/organization
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/organization")
public class OrganizationController extends BaseController {

    private final IOrganizationService organizationService;

    /**
     * 查询组织列表
     */
    @SaCheckPermission("user:organization:list")
    @GetMapping("/list")
    public TableDataInfo<OrganizationVo> list(OrganizationBo bo, PageQuery pageQuery) {
        return organizationService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出组织列表
     */
    @SaCheckPermission("user:organization:export")
    @Log(title = "组织", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(OrganizationBo bo, HttpServletResponse response) {
        List<OrganizationVo> list = organizationService.queryList(bo);
        ExcelUtil.exportExcel(list, "组织", OrganizationVo.class, response);
    }

    /**
     * 获取组织详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:organization:query")
    @GetMapping("/{id}")
    public R<OrganizationVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(organizationService.queryById(id));
    }

    /**
     * 新增组织
     */
    @SaCheckPermission("user:organization:add")
    @Log(title = "组织", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OrganizationBo bo) {
        return toAjax(organizationService.insertByBo(bo));
    }

    /**
     * 修改组织
     */
    @SaCheckPermission("user:organization:edit")
    @Log(title = "组织", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OrganizationBo bo) {
        return toAjax(organizationService.updateByBo(bo));
    }

    /**
     * 删除组织
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:organization:remove")
    @Log(title = "组织", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(organizationService.deleteWithValidByIds(List.of(ids), true));
    }
}
