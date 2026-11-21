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
import org.dromara.goods.domain.vo.SkuVo;
import org.dromara.goods.domain.bo.SkuBo;
import org.dromara.goods.service.ISkuService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品货品SKU
 * 前端访问路由地址为:/goods/sku
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/sku")
public class SkuController extends BaseController {

    private final ISkuService skuService;

    /**
     * 查询商品货品SKU列表
     */
    @SaCheckPermission("goods:sku:list")
    @GetMapping("/list")
    public TableDataInfo<SkuVo> list(SkuBo bo, PageQuery pageQuery) {
        return skuService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品货品SKU列表
     */
    @SaCheckPermission("goods:sku:export")
    @Log(title = "商品货品SKU", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SkuBo bo, HttpServletResponse response) {
        List<SkuVo> list = skuService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品货品SKU", SkuVo.class, response);
    }

    /**
     * 获取商品货品SKU详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:sku:query")
    @GetMapping("/{id}")
    public R<SkuVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(skuService.queryById(id));
    }

    /**
     * 新增商品货品SKU
     */
    @SaCheckPermission("goods:sku:add")
    @Log(title = "商品货品SKU", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SkuBo bo) {
        return toAjax(skuService.insertByBo(bo));
    }

    /**
     * 修改商品货品SKU
     */
    @SaCheckPermission("goods:sku:edit")
    @Log(title = "商品货品SKU", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SkuBo bo) {
        return toAjax(skuService.updateByBo(bo));
    }

    /**
     * 删除商品货品SKU
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:sku:remove")
    @Log(title = "商品货品SKU", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(skuService.deleteWithValidByIds(List.of(ids), true));
    }
}
