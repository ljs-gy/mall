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
import org.dromara.goods.domain.vo.CategoryFrontendVo;
import org.dromara.goods.domain.bo.CategoryFrontendBo;
import org.dromara.goods.service.ICategoryFrontendService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 前台商品分类
 * 前端访问路由地址为:/goods/categoryFrontend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/categoryFrontend")
public class CategoryFrontendController extends BaseController {

    private final ICategoryFrontendService categoryFrontendService;

    /**
     * 查询前台商品分类列表
     */
    @SaCheckPermission("goods:categoryFrontend:list")
    @GetMapping("/list")
    public TableDataInfo<CategoryFrontendVo> list(CategoryFrontendBo bo, PageQuery pageQuery) {
        return categoryFrontendService.queryPageList(bo, pageQuery);
    }

    /**
     * 前台分类树
     */
    @SaCheckPermission("goods:categoryFrontend:list")
    @GetMapping("/tree")
    public R<List<Tree<Long>>> tree() {
        return R.ok(categoryFrontendService.selectTree());
    }

    /**
     * 三级前台分类可选后台三级列表
     *
     * @param id 前台分类ID
     */
    @SaCheckPermission("goods:categoryFrontend:query")
    @GetMapping("/{id}/backendOptions")
    public R<List<CategoryBackendVo>> backendOptions(@NotNull(message = "主键不能为空")
                                                     @PathVariable("id") Long id) {
        return R.ok(categoryFrontendService.listBackendOptions(id));
    }

    /**
     * 导出前台商品分类列表
     */
    @SaCheckPermission("goods:categoryFrontend:export")
    @Log(title = "前台商品分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CategoryFrontendBo bo, HttpServletResponse response) {
        List<CategoryFrontendVo> list = categoryFrontendService.queryList(bo);
        ExcelUtil.exportExcel(list, "前台商品分类", CategoryFrontendVo.class, response);
    }

    /**
     * 获取前台商品分类详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:categoryFrontend:query")
    @GetMapping("/{id}")
    public R<CategoryFrontendVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(categoryFrontendService.queryById(id));
    }

    /**
     * 新增前台商品分类
     */
    @SaCheckPermission("goods:categoryFrontend:add")
    @Log(title = "前台商品分类", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CategoryFrontendBo bo) {
        return toAjax(categoryFrontendService.insertByBo(bo));
    }

    /**
     * 修改前台商品分类
     */
    @SaCheckPermission("goods:categoryFrontend:edit")
    @Log(title = "前台商品分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CategoryFrontendBo bo) {
        return toAjax(categoryFrontendService.updateByBo(bo));
    }

    /**
     * 删除前台商品分类
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:categoryFrontend:remove")
    @Log(title = "前台商品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(categoryFrontendService.deleteWithValidByIds(List.of(ids), true));
    }
}
