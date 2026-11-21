package org.dromara.pm.controller;

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
import org.dromara.pm.domain.vo.PromoGoodsSkuVo;
import org.dromara.pm.domain.bo.PromoGoodsSkuBo;
import org.dromara.pm.service.IPromoGoodsSkuService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 活动商品配置
 * 前端访问路由地址为:/pm/promoGoodsSku
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promoGoodsSku")
public class PromoGoodsSkuController extends BaseController {

    private final IPromoGoodsSkuService promoGoodsSkuService;

    /**
     * 查询活动商品配置列表
     */
    @SaCheckPermission("pm:promoGoodsSku:list")
    @GetMapping("/list")
    public TableDataInfo<PromoGoodsSkuVo> list(PromoGoodsSkuBo bo, PageQuery pageQuery) {
        return promoGoodsSkuService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出活动商品配置列表
     */
    @SaCheckPermission("pm:promoGoodsSku:export")
    @Log(title = "活动商品配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PromoGoodsSkuBo bo, HttpServletResponse response) {
        List<PromoGoodsSkuVo> list = promoGoodsSkuService.queryList(bo);
        ExcelUtil.exportExcel(list, "活动商品配置", PromoGoodsSkuVo.class, response);
    }

    /**
     * 获取活动商品配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:promoGoodsSku:query")
    @GetMapping("/{id}")
    public R<PromoGoodsSkuVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(promoGoodsSkuService.queryById(id));
    }

    /**
     * 新增活动商品配置
     */
    @SaCheckPermission("pm:promoGoodsSku:add")
    @Log(title = "活动商品配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PromoGoodsSkuBo bo) {
        return toAjax(promoGoodsSkuService.insertByBo(bo));
    }

    /**
     * 修改活动商品配置
     */
    @SaCheckPermission("pm:promoGoodsSku:edit")
    @Log(title = "活动商品配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PromoGoodsSkuBo bo) {
        return toAjax(promoGoodsSkuService.updateByBo(bo));
    }

    /**
     * 删除活动商品配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:promoGoodsSku:remove")
    @Log(title = "活动商品配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(promoGoodsSkuService.deleteWithValidByIds(List.of(ids), true));
    }
}
