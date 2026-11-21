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
import org.dromara.dis.domain.vo.DistLevelVo;
import org.dromara.dis.domain.bo.DistLevelBo;
import org.dromara.dis.service.IDistLevelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分销员等级
 * 前端访问路由地址为:/dis/level
 *
 * @author ljs
 * @date 2026-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/level")
public class DistLevelController extends BaseController {

    private final IDistLevelService distLevelService;

    /**
     * 查询分销员等级列表
     */
    @SaCheckPermission("dis:level:list")
    @GetMapping("/list")
    public TableDataInfo<DistLevelVo> list(DistLevelBo bo, PageQuery pageQuery) {
        return distLevelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分销员等级列表
     */
    @SaCheckPermission("dis:level:export")
    @Log(title = "分销员等级", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DistLevelBo bo, HttpServletResponse response) {
        List<DistLevelVo> list = distLevelService.queryList(bo);
        ExcelUtil.exportExcel(list, "分销员等级", DistLevelVo.class, response);
    }

    /**
     * 获取分销员等级详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:level:query")
    @GetMapping("/{id}")
    public R<DistLevelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(distLevelService.queryById(id));
    }

    /**
     * 新增分销员等级
     */
    @SaCheckPermission("dis:level:add")
    @Log(title = "分销员等级", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DistLevelBo bo) {
        return toAjax(distLevelService.insertByBo(bo));
    }

    /**
     * 修改分销员等级
     */
    @SaCheckPermission("dis:level:edit")
    @Log(title = "分销员等级", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DistLevelBo bo) {
        return toAjax(distLevelService.updateByBo(bo));
    }

    /**
     * 删除分销员等级
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:level:remove")
    @Log(title = "分销员等级", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(distLevelService.deleteWithValidByIds(List.of(ids), true));
    }
}
