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
import org.dromara.dis.domain.vo.DistSettingVo;
import org.dromara.dis.domain.bo.DistSettingBo;
import org.dromara.dis.service.IDistSettingService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分销基础配置
 * 前端访问路由地址为:/dis/setting
 *
 * @author ljs
 * @date 2026-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/setting")
public class DistSettingController extends BaseController {

    private final IDistSettingService distSettingService;

    /**
     * 查询分销基础配置列表
     */
    @SaCheckPermission("dis:setting:list")
    @GetMapping("/list")
    public TableDataInfo<DistSettingVo> list(DistSettingBo bo, PageQuery pageQuery) {
        return distSettingService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分销基础配置列表
     */
    @SaCheckPermission("dis:setting:export")
    @Log(title = "分销基础配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DistSettingBo bo, HttpServletResponse response) {
        List<DistSettingVo> list = distSettingService.queryList(bo);
        ExcelUtil.exportExcel(list, "分销基础配置", DistSettingVo.class, response);
    }

    /**
     * 获取分销基础配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:setting:query")
    @GetMapping("/{id}")
    public R<DistSettingVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(distSettingService.queryById(id));
    }

    /**
     * 新增分销基础配置
     */
    @SaCheckPermission("dis:setting:add")
    @Log(title = "分销基础配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DistSettingBo bo) {
        return toAjax(distSettingService.insertByBo(bo));
    }

    /**
     * 修改分销基础配置
     */
    @SaCheckPermission("dis:setting:edit")
    @Log(title = "分销基础配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DistSettingBo bo) {
        return toAjax(distSettingService.updateByBo(bo));
    }

    /**
     * 删除分销基础配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:setting:remove")
    @Log(title = "分销基础配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(distSettingService.deleteWithValidByIds(List.of(ids), true));
    }
}
