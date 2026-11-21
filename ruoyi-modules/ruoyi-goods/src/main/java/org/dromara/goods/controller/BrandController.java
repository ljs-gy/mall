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
import org.dromara.goods.domain.vo.BrandVo;
import org.dromara.goods.domain.bo.BrandBo;
import org.dromara.goods.service.IBrandService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品品牌
 * 前端访问路由地址为:/goods/brand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/brand")
public class BrandController extends BaseController {

    private final IBrandService brandService;

    /**
     * 查询商品品牌列表
     */
    @SaCheckPermission("goods:brand:list")
    @GetMapping("/list")
    public TableDataInfo<BrandVo> list(BrandBo bo, PageQuery pageQuery) {
        return brandService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品品牌列表
     */
    @SaCheckPermission("goods:brand:export")
    @Log(title = "商品品牌", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BrandBo bo, HttpServletResponse response) {
        List<BrandVo> list = brandService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品品牌", BrandVo.class, response);
    }

    /**
     * 获取商品品牌详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:brand:query")
    @GetMapping("/{id}")
    public R<BrandVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(brandService.queryById(id));
    }

    /**
     * 新增商品品牌
     */
    @SaCheckPermission("goods:brand:add")
    @Log(title = "商品品牌", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BrandBo bo) {
        return toAjax(brandService.insertByBo(bo));
    }

    /**
     * 修改商品品牌
     */
    @SaCheckPermission("goods:brand:edit")
    @Log(title = "商品品牌", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BrandBo bo) {
        return toAjax(brandService.updateByBo(bo));
    }

    /**
     * 删除商品品牌
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:brand:remove")
    @Log(title = "商品品牌", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(brandService.deleteWithValidByIds(List.of(ids), true));
    }
}
