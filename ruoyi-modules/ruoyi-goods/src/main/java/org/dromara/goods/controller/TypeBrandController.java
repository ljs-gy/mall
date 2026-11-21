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
import org.dromara.goods.domain.vo.TypeBrandVo;
import org.dromara.goods.domain.bo.TypeBrandBo;
import org.dromara.goods.service.ITypeBrandService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品类型与品牌关系
 * 前端访问路由地址为:/goods/typeBrand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/typeBrand")
public class TypeBrandController extends BaseController {

    private final ITypeBrandService typeBrandService;

    /**
     * 查询商品类型与品牌关系列表
     */
    @SaCheckPermission("goods:typeBrand:list")
    @GetMapping("/list")
    public TableDataInfo<TypeBrandVo> list(TypeBrandBo bo, PageQuery pageQuery) {
        return typeBrandService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品类型与品牌关系列表
     */
    @SaCheckPermission("goods:typeBrand:export")
    @Log(title = "商品类型与品牌关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TypeBrandBo bo, HttpServletResponse response) {
        List<TypeBrandVo> list = typeBrandService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品类型与品牌关系", TypeBrandVo.class, response);
    }

    /**
     * 获取商品类型与品牌关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:typeBrand:query")
    @GetMapping("/{id}")
    public R<TypeBrandVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(typeBrandService.queryById(id));
    }

    /**
     * 新增商品类型与品牌关系
     */
    @SaCheckPermission("goods:typeBrand:add")
    @Log(title = "商品类型与品牌关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody TypeBrandBo bo) {
        return toAjax(typeBrandService.insertByBo(bo));
    }

    /**
     * 修改商品类型与品牌关系
     */
    @SaCheckPermission("goods:typeBrand:edit")
    @Log(title = "商品类型与品牌关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody TypeBrandBo bo) {
        return toAjax(typeBrandService.updateByBo(bo));
    }

    /**
     * 删除商品类型与品牌关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:typeBrand:remove")
    @Log(title = "商品类型与品牌关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(typeBrandService.deleteWithValidByIds(List.of(ids), true));
    }
}
