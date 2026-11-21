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
import org.dromara.order.domain.vo.OrderVo;
import org.dromara.order.domain.bo.OrderBo;
import org.dromara.order.service.IOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 订单主
 * 前端访问路由地址为:/order/order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {

    private final IOrderService orderService;

    /**
     * 查询订单主列表
     */
    @SaCheckPermission("order:order:list")
    @GetMapping("/list")
    public TableDataInfo<OrderVo> list(OrderBo bo, PageQuery pageQuery) {
        return orderService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出订单主列表
     */
    @SaCheckPermission("order:order:export")
    @Log(title = "订单主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(OrderBo bo, HttpServletResponse response) {
        List<OrderVo> list = orderService.queryList(bo);
        ExcelUtil.exportExcel(list, "订单主", OrderVo.class, response);
    }

    /**
     * 获取订单主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:order:query")
    @GetMapping("/{id}")
    public R<OrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(orderService.queryById(id));
    }

    /**
     * 新增订单主
     */
    @SaCheckPermission("order:order:add")
    @Log(title = "订单主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OrderBo bo) {
        return toAjax(orderService.insertByBo(bo));
    }

    /**
     * 修改订单主
     */
    @SaCheckPermission("order:order:edit")
    @Log(title = "订单主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OrderBo bo) {
        return toAjax(orderService.updateByBo(bo));
    }

    /**
     * 删除订单主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:order:remove")
    @Log(title = "订单主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(orderService.deleteWithValidByIds(List.of(ids), true));
    }
}
