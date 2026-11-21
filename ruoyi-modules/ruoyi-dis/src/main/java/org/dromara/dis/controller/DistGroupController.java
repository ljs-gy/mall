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
import org.dromara.dis.domain.vo.DistGroupVo;
import org.dromara.dis.domain.bo.DistGroupBo;
import org.dromara.dis.service.IDistGroupService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分销员分组
 * 前端访问路由地址为:/dis/group
 *
 * @author ljs
 * @date 2026-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class DistGroupController extends BaseController {

    private final IDistGroupService distGroupService;

    /**
     * 查询分销员分组列表
     */
    @SaCheckPermission("dis:group:list")
    @GetMapping("/list")
    public TableDataInfo<DistGroupVo> list(DistGroupBo bo, PageQuery pageQuery) {
        return distGroupService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分销员分组列表
     */
    @SaCheckPermission("dis:group:export")
    @Log(title = "分销员分组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DistGroupBo bo, HttpServletResponse response) {
        List<DistGroupVo> list = distGroupService.queryList(bo);
        ExcelUtil.exportExcel(list, "分销员分组", DistGroupVo.class, response);
    }

    /**
     * 获取分销员分组详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:group:query")
    @GetMapping("/{id}")
    public R<DistGroupVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(distGroupService.queryById(id));
    }

    /**
     * 新增分销员分组
     */
    @SaCheckPermission("dis:group:add")
    @Log(title = "分销员分组", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DistGroupBo bo) {
        return toAjax(distGroupService.insertByBo(bo));
    }

    /**
     * 修改分销员分组
     */
    @SaCheckPermission("dis:group:edit")
    @Log(title = "分销员分组", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DistGroupBo bo) {
        return toAjax(distGroupService.updateByBo(bo));
    }

    /**
     * 删除分销员分组
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:group:remove")
    @Log(title = "分销员分组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(distGroupService.deleteWithValidByIds(List.of(ids), true));
    }
}
