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
import org.dromara.goods.domain.vo.CategoryFrontBackRelVo;
import org.dromara.goods.domain.bo.CategoryFrontBackRelBo;
import org.dromara.goods.service.ICategoryFrontBackRelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 前后台分类映射关系
 * 前端访问路由地址为:/goods/categoryFrontBackRel
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/categoryFrontBackRel")
public class CategoryFrontBackRelController extends BaseController {

    private final ICategoryFrontBackRelService categoryFrontBackRelService;

    /**
     * 查询前后台分类映射关系列表
     */
    @SaCheckPermission("goods:categoryFrontBackRel:list")
    @GetMapping("/list")
    public TableDataInfo<CategoryFrontBackRelVo> list(CategoryFrontBackRelBo bo, PageQuery pageQuery) {
        return categoryFrontBackRelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出前后台分类映射关系列表
     */
    @SaCheckPermission("goods:categoryFrontBackRel:export")
    @Log(title = "前后台分类映射关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CategoryFrontBackRelBo bo, HttpServletResponse response) {
        List<CategoryFrontBackRelVo> list = categoryFrontBackRelService.queryList(bo);
        ExcelUtil.exportExcel(list, "前后台分类映射关系", CategoryFrontBackRelVo.class, response);
    }

    /**
     * 获取前后台分类映射关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:categoryFrontBackRel:query")
    @GetMapping("/{id}")
    public R<CategoryFrontBackRelVo> getInfo(@NotNull(message = "主键不能为空")
                                             @PathVariable("id") Long id) {
        return R.ok(categoryFrontBackRelService.queryById(id));
    }

    /**
     * 新增前后台分类映射关系
     */
    @SaCheckPermission("goods:categoryFrontBackRel:add")
    @Log(title = "前后台分类映射关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CategoryFrontBackRelBo bo) {
        return toAjax(categoryFrontBackRelService.insertByBo(bo));
    }

    /**
     * 修改前后台分类映射关系
     */
    @SaCheckPermission("goods:categoryFrontBackRel:edit")
    @Log(title = "前后台分类映射关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CategoryFrontBackRelBo bo) {
        return toAjax(categoryFrontBackRelService.updateByBo(bo));
    }

    /**
     * 删除前后台分类映射关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:categoryFrontBackRel:remove")
    @Log(title = "前后台分类映射关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(categoryFrontBackRelService.deleteWithValidByIds(List.of(ids), true));
    }
}
