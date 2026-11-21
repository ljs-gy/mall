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
import org.dromara.dis.domain.vo.DistRelationVo;
import org.dromara.dis.domain.bo.DistRelationBo;
import org.dromara.dis.service.IDistRelationService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分销上下级关系绑定
 * 前端访问路由地址为:/dis/relation
 *
 * @author ljs
 * @date 2026-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/relation")
public class DistRelationController extends BaseController {

    private final IDistRelationService distRelationService;

    /**
     * 查询分销上下级关系绑定列表
     */
    @SaCheckPermission("dis:relation:list")
    @GetMapping("/list")
    public TableDataInfo<DistRelationVo> list(DistRelationBo bo, PageQuery pageQuery) {
        return distRelationService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分销上下级关系绑定列表
     */
    @SaCheckPermission("dis:relation:export")
    @Log(title = "分销上下级关系绑定", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DistRelationBo bo, HttpServletResponse response) {
        List<DistRelationVo> list = distRelationService.queryList(bo);
        ExcelUtil.exportExcel(list, "分销上下级关系绑定", DistRelationVo.class, response);
    }

    /**
     * 获取分销上下级关系绑定详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:relation:query")
    @GetMapping("/{id}")
    public R<DistRelationVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(distRelationService.queryById(id));
    }

    /**
     * 新增分销上下级关系绑定
     */
    @SaCheckPermission("dis:relation:add")
    @Log(title = "分销上下级关系绑定", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DistRelationBo bo) {
        return toAjax(distRelationService.insertByBo(bo));
    }

    /**
     * 修改分销上下级关系绑定
     */
    @SaCheckPermission("dis:relation:edit")
    @Log(title = "分销上下级关系绑定", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DistRelationBo bo) {
        return toAjax(distRelationService.updateByBo(bo));
    }

    /**
     * 删除分销上下级关系绑定
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:relation:remove")
    @Log(title = "分销上下级关系绑定", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(distRelationService.deleteWithValidByIds(List.of(ids), true));
    }
}
