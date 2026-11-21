package org.dromara.goods.controller;

import java.util.List;

import cn.hutool.core.lang.tree.Tree;
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
import org.dromara.goods.domain.vo.CategoryBackendVo;
import org.dromara.goods.domain.bo.CategoryBackendBo;
import org.dromara.goods.service.ICategoryBackendService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 后台商品分类
 * 前端访问路由地址为:/goods/categoryBackend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/categoryBackend")
public class CategoryBackendController extends BaseController {

    private final ICategoryBackendService categoryBackendService;

    /**
     * 查询后台商品分类列表
     */
    @SaCheckPermission("goods:categoryBackend:list")
    @GetMapping("/list")
    public TableDataInfo<CategoryBackendVo> list(CategoryBackendBo bo, PageQuery pageQuery) {
        return categoryBackendService.queryPageList(bo, pageQuery);
    }

    /**
     * 后台分类树
     */
    @SaCheckPermission("goods:categoryBackend:list")
    @GetMapping("/tree")
    public R<List<Tree<Long>>> tree() {
        return R.ok(categoryBackendService.selectTree());
    }

    /**
     * 导出后台商品分类列表
     */
    @SaCheckPermission("goods:categoryBackend:export")
    @Log(title = "后台商品分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CategoryBackendBo bo, HttpServletResponse response) {
        List<CategoryBackendVo> list = categoryBackendService.queryList(bo);
        ExcelUtil.exportExcel(list, "后台商品分类", CategoryBackendVo.class, response);
    }

    /**
     * 获取后台商品分类详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:categoryBackend:query")
    @GetMapping("/{id}")
    public R<CategoryBackendVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(categoryBackendService.queryById(id));
    }

    /**
     * 新增后台商品分类
     */
    @SaCheckPermission("goods:categoryBackend:add")
    @Log(title = "后台商品分类", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CategoryBackendBo bo) {
        return toAjax(categoryBackendService.insertByBo(bo));
    }

    /**
     * 修改后台商品分类
     */
    @SaCheckPermission("goods:categoryBackend:edit")
    @Log(title = "后台商品分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CategoryBackendBo bo) {
        return toAjax(categoryBackendService.updateByBo(bo));
    }

    /**
     * 删除后台商品分类
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:categoryBackend:remove")
    @Log(title = "后台商品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(categoryBackendService.deleteWithValidByIds(List.of(ids), true));
    }
}
