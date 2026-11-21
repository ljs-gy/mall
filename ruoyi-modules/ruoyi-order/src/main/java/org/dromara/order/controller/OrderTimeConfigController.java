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
import org.dromara.order.domain.vo.OrderTimeConfigVo;
import org.dromara.order.domain.bo.OrderTimeConfigBo;
import org.dromara.order.service.IOrderTimeConfigService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 订单交易时效配置
 * 前端访问路由地址为:/order/orderTimeConfig
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/orderTimeConfig")
public class OrderTimeConfigController extends BaseController {

    private final IOrderTimeConfigService orderTimeConfigService;

    /**
     * 查询订单交易时效配置列表
     */
    @SaCheckPermission("order:orderTimeConfig:list")
    @GetMapping("/list")
    public TableDataInfo<OrderTimeConfigVo> list(OrderTimeConfigBo bo, PageQuery pageQuery) {
        return orderTimeConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出订单交易时效配置列表
     */
    @SaCheckPermission("order:orderTimeConfig:export")
    @Log(title = "订单交易时效配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(OrderTimeConfigBo bo, HttpServletResponse response) {
        List<OrderTimeConfigVo> list = orderTimeConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "订单交易时效配置", OrderTimeConfigVo.class, response);
    }

    /**
     * 获取订单交易时效配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:orderTimeConfig:query")
    @GetMapping("/{id}")
    public R<OrderTimeConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(orderTimeConfigService.queryById(id));
    }

    /**
     * 新增订单交易时效配置
     */
    @SaCheckPermission("order:orderTimeConfig:add")
    @Log(title = "订单交易时效配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody OrderTimeConfigBo bo) {
        return toAjax(orderTimeConfigService.insertByBo(bo));
    }

    /**
     * 修改订单交易时效配置
     */
    @SaCheckPermission("order:orderTimeConfig:edit")
    @Log(title = "订单交易时效配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody OrderTimeConfigBo bo) {
        return toAjax(orderTimeConfigService.updateByBo(bo));
    }

    /**
     * 删除订单交易时效配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:orderTimeConfig:remove")
    @Log(title = "订单交易时效配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(orderTimeConfigService.deleteWithValidByIds(List.of(ids), true));
    }
}
