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
import org.dromara.goods.domain.vo.AttributeVo;
import org.dromara.goods.domain.bo.AttributeBo;
import org.dromara.goods.service.IAttributeService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品属性
 * 前端访问路由地址为:/goods/attribute
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/attribute")
public class AttributeController extends BaseController {

    private final IAttributeService attributeService;

    /**
     * 查询商品属性列表
     */
    @SaCheckPermission("goods:attribute:list")
    @GetMapping("/list")
    public TableDataInfo<AttributeVo> list(AttributeBo bo, PageQuery pageQuery) {
        return attributeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品属性列表
     */
    @SaCheckPermission("goods:attribute:export")
    @Log(title = "商品属性", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AttributeBo bo, HttpServletResponse response) {
        List<AttributeVo> list = attributeService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品属性", AttributeVo.class, response);
    }

    /**
     * 获取商品属性详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:attribute:query")
    @GetMapping("/{id}")
    public R<AttributeVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(attributeService.queryById(id));
    }

    /**
     * 新增商品属性
     */
    @SaCheckPermission("goods:attribute:add")
    @Log(title = "商品属性", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AttributeBo bo) {
        return toAjax(attributeService.insertByBo(bo));
    }

    /**
     * 修改商品属性
     */
    @SaCheckPermission("goods:attribute:edit")
    @Log(title = "商品属性", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AttributeBo bo) {
        return toAjax(attributeService.updateByBo(bo));
    }

    /**
     * 删除商品属性
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:attribute:remove")
    @Log(title = "商品属性", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(attributeService.deleteWithValidByIds(List.of(ids), true));
    }
}
