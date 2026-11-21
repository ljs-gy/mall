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
import org.dromara.goods.domain.vo.SpuAttributeValueVo;
import org.dromara.goods.domain.bo.SpuAttributeValueBo;
import org.dromara.goods.service.ISpuAttributeValueService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品SPU属性值
 * 前端访问路由地址为:/goods/spuAttributeValue
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/spuAttributeValue")
public class SpuAttributeValueController extends BaseController {

    private final ISpuAttributeValueService spuAttributeValueService;

    /**
     * 查询商品SPU属性值列表
     */
    @SaCheckPermission("goods:spuAttributeValue:list")
    @GetMapping("/list")
    public TableDataInfo<SpuAttributeValueVo> list(SpuAttributeValueBo bo, PageQuery pageQuery) {
        return spuAttributeValueService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品SPU属性值列表
     */
    @SaCheckPermission("goods:spuAttributeValue:export")
    @Log(title = "商品SPU属性值", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SpuAttributeValueBo bo, HttpServletResponse response) {
        List<SpuAttributeValueVo> list = spuAttributeValueService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品SPU属性值", SpuAttributeValueVo.class, response);
    }

    /**
     * 获取商品SPU属性值详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:spuAttributeValue:query")
    @GetMapping("/{id}")
    public R<SpuAttributeValueVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(spuAttributeValueService.queryById(id));
    }

    /**
     * 新增商品SPU属性值
     */
    @SaCheckPermission("goods:spuAttributeValue:add")
    @Log(title = "商品SPU属性值", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpuAttributeValueBo bo) {
        return toAjax(spuAttributeValueService.insertByBo(bo));
    }

    /**
     * 修改商品SPU属性值
     */
    @SaCheckPermission("goods:spuAttributeValue:edit")
    @Log(title = "商品SPU属性值", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpuAttributeValueBo bo) {
        return toAjax(spuAttributeValueService.updateByBo(bo));
    }

    /**
     * 删除商品SPU属性值
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:spuAttributeValue:remove")
    @Log(title = "商品SPU属性值", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(spuAttributeValueService.deleteWithValidByIds(List.of(ids), true));
    }
}
