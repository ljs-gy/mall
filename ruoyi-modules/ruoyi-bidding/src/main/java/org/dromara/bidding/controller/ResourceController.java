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
import org.dromara.bidding.domain.vo.ResourceVo;
import org.dromara.bidding.domain.bo.ResourceBo;
import org.dromara.bidding.service.IResourceService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 竞价资源主
 * 前端访问路由地址为:/bidding/resource
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource")
public class ResourceController extends BaseController {

    private final IResourceService resourceService;

    /**
     * 查询竞价资源主列表
     */
    @SaCheckPermission("bidding:resource:list")
    @GetMapping("/list")
    public TableDataInfo<ResourceVo> list(ResourceBo bo, PageQuery pageQuery) {
        return resourceService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出竞价资源主列表
     */
    @SaCheckPermission("bidding:resource:export")
    @Log(title = "竞价资源主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ResourceBo bo, HttpServletResponse response) {
        List<ResourceVo> list = resourceService.queryList(bo);
        ExcelUtil.exportExcel(list, "竞价资源主", ResourceVo.class, response);
    }

    /**
     * 获取竞价资源主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("bidding:resource:query")
    @GetMapping("/{id}")
    public R<ResourceVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(resourceService.queryById(id));
    }

    /**
     * 新增竞价资源主
     */
    @SaCheckPermission("bidding:resource:add")
    @Log(title = "竞价资源主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ResourceBo bo) {
        return toAjax(resourceService.insertByBo(bo));
    }

    /**
     * 修改竞价资源主
     */
    @SaCheckPermission("bidding:resource:edit")
    @Log(title = "竞价资源主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ResourceBo bo) {
        return toAjax(resourceService.updateByBo(bo));
    }

    /**
     * 删除竞价资源主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("bidding:resource:remove")
    @Log(title = "竞价资源主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(resourceService.deleteWithValidByIds(List.of(ids), true));
    }
}
