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
import org.dromara.goods.domain.vo.SpuVo;
import org.dromara.goods.domain.bo.AuditGoodsBo;
import org.dromara.goods.domain.bo.BatchSaleStatusBo;
import org.dromara.goods.domain.bo.PublishGoodsBo;
import org.dromara.goods.domain.bo.SaleStatusBo;
import org.dromara.goods.domain.bo.SpuBo;
import org.dromara.goods.service.IGoodsPublishService;
import org.dromara.goods.service.ISpuService;
import org.dromara.common.mybatis.core.page.TableDataInfo;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/spu")
public class SpuController extends BaseController {

    private final ISpuService spuService;
    private final IGoodsPublishService goodsPublishService;

    /**
     * 查询商品SPU主列表
     */
    @SaCheckPermission("goods:spu:list")
    @GetMapping("/list")
    public TableDataInfo<SpuVo> list(SpuBo bo, PageQuery pageQuery) {
        return spuService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品SPU主列表
     */
    @SaCheckPermission("goods:spu:export")
    @Log(title = "商品SPU主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SpuBo bo, HttpServletResponse response) {
        List<SpuVo> list = spuService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品SPU主", SpuVo.class, response);
    }

    /**
     * 获取商品SPU主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:spu:query")
    @GetMapping("/{id}")
    public R<SpuVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(spuService.queryById(id));
    }

    /**
     * 新增商品SPU主
     */
    @SaCheckPermission("goods:spu:add")
    @Log(title = "商品SPU主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpuBo bo) {
        return toAjax(spuService.insertByBo(bo));
    }

    /**
     * 发布商品
     */
    @SaCheckPermission("goods:spu:add")
    @Log(title = "发布商品", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/publish")
    public R<Long> publish(@Validated @RequestBody PublishGoodsBo bo) {
        return R.ok(goodsPublishService.publish(bo));
    }

    /**
     * 编辑商品
     */
    @SaCheckPermission("goods:spu:edit")
    @Log(title = "编辑商品", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/publish")
    public R<Long> editPublish(@Validated @RequestBody PublishGoodsBo bo) {
        return R.ok(goodsPublishService.edit(bo));
    }

    /**
     * 审核商品
     */
    @SaCheckPermission("goods:spu:edit")
    @Log(title = "审核商品", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/audit")
    public R<Void> audit(@Validated @RequestBody AuditGoodsBo bo) {
        return toAjax(goodsPublishService.audit(bo));
    }

    /**
     * 上下架
     */
    @SaCheckPermission("goods:spu:edit")
    @Log(title = "商品上下架", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/saleStatus")
    public R<Void> saleStatus(@Validated @RequestBody SaleStatusBo bo) {
        return toAjax(goodsPublishService.updateSaleStatus(bo));
    }

    /**
     * 批量上下架
     */
    @SaCheckPermission("goods:spu:edit")
    @Log(title = "商品批量上下架", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/saleStatus/batch")
    public R<Void> batchSaleStatus(@Validated @RequestBody BatchSaleStatusBo bo) {
        return toAjax(goodsPublishService.batchUpdateSaleStatus(bo));
    }

    /**
     * 修改商品SPU主
     */
    @SaCheckPermission("goods:spu:edit")
    @Log(title = "商品SPU主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpuBo bo) {
        return toAjax(spuService.updateByBo(bo));
    }

    /**
     * 删除商品SPU主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:spu:remove")
    @Log(title = "商品SPU主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(spuService.deleteWithValidByIds(List.of(ids), true));
    }
}
