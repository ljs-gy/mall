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
import org.dromara.goods.domain.vo.AttributeOptionVo;
import org.dromara.goods.domain.bo.AttributeOptionBo;
import org.dromara.goods.service.IAttributeOptionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品属性选项
 * 前端访问路由地址为:/goods/attributeOption
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/attributeOption")
public class AttributeOptionController extends BaseController {

    private final IAttributeOptionService attributeOptionService;

    /**
     * 查询商品属性选项列表
     */
    @SaCheckPermission("goods:attributeOption:list")
    @GetMapping("/list")
    public TableDataInfo<AttributeOptionVo> list(AttributeOptionBo bo, PageQuery pageQuery) {
        return attributeOptionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品属性选项列表
     */
    @SaCheckPermission("goods:attributeOption:export")
    @Log(title = "商品属性选项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AttributeOptionBo bo, HttpServletResponse response) {
        List<AttributeOptionVo> list = attributeOptionService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品属性选项", AttributeOptionVo.class, response);
    }

    /**
     * 获取商品属性选项详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:attributeOption:query")
    @GetMapping("/{id}")
    public R<AttributeOptionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(attributeOptionService.queryById(id));
    }

    /**
     * 新增商品属性选项
     */
    @SaCheckPermission("goods:attributeOption:add")
    @Log(title = "商品属性选项", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AttributeOptionBo bo) {
        return toAjax(attributeOptionService.insertByBo(bo));
    }

    /**
     * 修改商品属性选项
     */
    @SaCheckPermission("goods:attributeOption:edit")
    @Log(title = "商品属性选项", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AttributeOptionBo bo) {
        return toAjax(attributeOptionService.updateByBo(bo));
    }

    /**
     * 删除商品属性选项
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:attributeOption:remove")
    @Log(title = "商品属性选项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(attributeOptionService.deleteWithValidByIds(List.of(ids), true));
    }
}
