package org.dromara.goods.controller;

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
import org.dromara.goods.domain.vo.SpecGroupVo;
import org.dromara.goods.domain.bo.SpecGroupBo;
import org.dromara.goods.service.ISpecGroupService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品规格组
 * 前端访问路由地址为:/goods/specGroup
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/specGroup")
public class SpecGroupController extends BaseController {

    private final ISpecGroupService specGroupService;

    /**
     * 查询商品规格组列表
     */
    @SaCheckPermission("goods:specGroup:list")
    @GetMapping("/list")
    public TableDataInfo<SpecGroupVo> list(SpecGroupBo bo, PageQuery pageQuery) {
        return specGroupService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品规格组列表
     */
    @SaCheckPermission("goods:specGroup:export")
    @Log(title = "商品规格组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SpecGroupBo bo, HttpServletResponse response) {
        List<SpecGroupVo> list = specGroupService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品规格组", SpecGroupVo.class, response);
    }

    /**
     * 获取商品规格组详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:specGroup:query")
    @GetMapping("/{id}")
    public R<SpecGroupVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(specGroupService.queryById(id));
    }

    /**
     * 新增商品规格组
     */
    @SaCheckPermission("goods:specGroup:add")
    @Log(title = "商品规格组", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpecGroupBo bo) {
        return toAjax(specGroupService.insertByBo(bo));
    }

    /**
     * 修改商品规格组
     */
    @SaCheckPermission("goods:specGroup:edit")
    @Log(title = "商品规格组", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecGroupBo bo) {
        return toAjax(specGroupService.updateByBo(bo));
    }

    /**
     * 删除商品规格组
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:specGroup:remove")
    @Log(title = "商品规格组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(specGroupService.deleteWithValidByIds(List.of(ids), true));
    }
}
