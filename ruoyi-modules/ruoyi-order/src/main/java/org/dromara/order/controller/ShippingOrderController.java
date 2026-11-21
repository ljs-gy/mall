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
import org.dromara.order.domain.vo.ShippingOrderVo;
import org.dromara.order.domain.bo.ShippingOrderBo;
import org.dromara.order.service.IShippingOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 发货单
 * 前端访问路由地址为:/order/shippingOrder
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/shippingOrder")
public class ShippingOrderController extends BaseController {

    private final IShippingOrderService shippingOrderService;

    /**
     * 查询发货单列表
     */
    @SaCheckPermission("order:shippingOrder:list")
    @GetMapping("/list")
    public TableDataInfo<ShippingOrderVo> list(ShippingOrderBo bo, PageQuery pageQuery) {
        return shippingOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出发货单列表
     */
    @SaCheckPermission("order:shippingOrder:export")
    @Log(title = "发货单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ShippingOrderBo bo, HttpServletResponse response) {
        List<ShippingOrderVo> list = shippingOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "发货单", ShippingOrderVo.class, response);
    }

    /**
     * 获取发货单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:shippingOrder:query")
    @GetMapping("/{id}")
    public R<ShippingOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(shippingOrderService.queryById(id));
    }

    /**
     * 新增发货单
     */
    @SaCheckPermission("order:shippingOrder:add")
    @Log(title = "发货单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ShippingOrderBo bo) {
        return toAjax(shippingOrderService.insertByBo(bo));
    }

    /**
     * 修改发货单
     */
    @SaCheckPermission("order:shippingOrder:edit")
    @Log(title = "发货单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ShippingOrderBo bo) {
        return toAjax(shippingOrderService.updateByBo(bo));
    }

    /**
     * 删除发货单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:shippingOrder:remove")
    @Log(title = "发货单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(shippingOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
