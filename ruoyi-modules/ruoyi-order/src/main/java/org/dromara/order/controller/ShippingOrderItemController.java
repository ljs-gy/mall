package org.dromara.order.controller;

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
import org.dromara.order.domain.vo.ShippingOrderItemVo;
import org.dromara.order.domain.bo.ShippingOrderItemBo;
import org.dromara.order.service.IShippingOrderItemService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 发货单包裹明细
 * 前端访问路由地址为:/order/shippingOrderItem
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/shippingOrderItem")
public class ShippingOrderItemController extends BaseController {

    private final IShippingOrderItemService shippingOrderItemService;

    /**
     * 查询发货单包裹明细列表
     */
    @SaCheckPermission("order:shippingOrderItem:list")
    @GetMapping("/list")
    public TableDataInfo<ShippingOrderItemVo> list(ShippingOrderItemBo bo, PageQuery pageQuery) {
        return shippingOrderItemService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出发货单包裹明细列表
     */
    @SaCheckPermission("order:shippingOrderItem:export")
    @Log(title = "发货单包裹明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ShippingOrderItemBo bo, HttpServletResponse response) {
        List<ShippingOrderItemVo> list = shippingOrderItemService.queryList(bo);
        ExcelUtil.exportExcel(list, "发货单包裹明细", ShippingOrderItemVo.class, response);
    }

    /**
     * 获取发货单包裹明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:shippingOrderItem:query")
    @GetMapping("/{id}")
    public R<ShippingOrderItemVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(shippingOrderItemService.queryById(id));
    }

    /**
     * 新增发货单包裹明细
     */
    @SaCheckPermission("order:shippingOrderItem:add")
    @Log(title = "发货单包裹明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ShippingOrderItemBo bo) {
        return toAjax(shippingOrderItemService.insertByBo(bo));
    }

    /**
     * 修改发货单包裹明细
     */
    @SaCheckPermission("order:shippingOrderItem:edit")
    @Log(title = "发货单包裹明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ShippingOrderItemBo bo) {
        return toAjax(shippingOrderItemService.updateByBo(bo));
    }

    /**
     * 删除发货单包裹明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:shippingOrderItem:remove")
    @Log(title = "发货单包裹明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(shippingOrderItemService.deleteWithValidByIds(List.of(ids), true));
    }
}
