package org.dromara.dis.controller;

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
import org.dromara.dis.domain.vo.DistUserGroupVo;
import org.dromara.dis.domain.bo.DistUserGroupBo;
import org.dromara.dis.service.IDistUserGroupService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分销员与分组关系
 * 前端访问路由地址为:/dis/userGroup
 *
 * @author ljs
 * @date 2026-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userGroup")
public class DistUserGroupController extends BaseController {

    private final IDistUserGroupService distUserGroupService;

    /**
     * 查询分销员与分组关系列表
     */
    @SaCheckPermission("dis:userGroup:list")
    @GetMapping("/list")
    public TableDataInfo<DistUserGroupVo> list(DistUserGroupBo bo, PageQuery pageQuery) {
        return distUserGroupService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分销员与分组关系列表
     */
    @SaCheckPermission("dis:userGroup:export")
    @Log(title = "分销员与分组关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DistUserGroupBo bo, HttpServletResponse response) {
        List<DistUserGroupVo> list = distUserGroupService.queryList(bo);
        ExcelUtil.exportExcel(list, "分销员与分组关系", DistUserGroupVo.class, response);
    }

    /**
     * 获取分销员与分组关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:userGroup:query")
    @GetMapping("/{id}")
    public R<DistUserGroupVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(distUserGroupService.queryById(id));
    }

    /**
     * 新增分销员与分组关系
     */
    @SaCheckPermission("dis:userGroup:add")
    @Log(title = "分销员与分组关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DistUserGroupBo bo) {
        return toAjax(distUserGroupService.insertByBo(bo));
    }

    /**
     * 修改分销员与分组关系
     */
    @SaCheckPermission("dis:userGroup:edit")
    @Log(title = "分销员与分组关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DistUserGroupBo bo) {
        return toAjax(distUserGroupService.updateByBo(bo));
    }

    /**
     * 删除分销员与分组关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:userGroup:remove")
    @Log(title = "分销员与分组关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(distUserGroupService.deleteWithValidByIds(List.of(ids), true));
    }
}
