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
import org.dromara.goods.domain.vo.TypeTemplateVo;
import org.dromara.goods.domain.vo.TypeVo;
import org.dromara.goods.domain.bo.TypeBo;
import org.dromara.goods.service.ITypeService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品类型
 * 前端访问路由地址为:/goods/type
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/type")
public class TypeController extends BaseController {

    private final ITypeService typeService;

    /**
     * 查询商品类型列表
     */
    @SaCheckPermission("goods:type:list")
    @GetMapping("/list")
    public TableDataInfo<TypeVo> list(TypeBo bo, PageQuery pageQuery) {
        return typeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品类型列表
     */
    @SaCheckPermission("goods:type:export")
    @Log(title = "商品类型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TypeBo bo, HttpServletResponse response) {
        List<TypeVo> list = typeService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品类型", TypeVo.class, response);
    }

    /**
     * 获取商品类型详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:type:query")
    @GetMapping("/{id}")
    public R<TypeVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(typeService.queryById(id));
    }

    /**
     * 类型发布模板（品牌/规格/参数/属性）
     *
     * @param id 类型ID
     */
    @SaCheckPermission("goods:type:query")
    @GetMapping("/{id}/template")
    public R<TypeTemplateVo> template(@NotNull(message = "主键不能为空")
                                      @PathVariable("id") Long id) {
        return R.ok(typeService.queryTemplate(id));
    }

    /**
     * 新增商品类型
     */
    @SaCheckPermission("goods:type:add")
    @Log(title = "商品类型", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody TypeBo bo) {
        return toAjax(typeService.insertByBo(bo));
    }

    /**
     * 修改商品类型
     */
    @SaCheckPermission("goods:type:edit")
    @Log(title = "商品类型", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody TypeBo bo) {
        return toAjax(typeService.updateByBo(bo));
    }

    /**
     * 删除商品类型
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:type:remove")
    @Log(title = "商品类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(typeService.deleteWithValidByIds(List.of(ids), true));
    }
}
