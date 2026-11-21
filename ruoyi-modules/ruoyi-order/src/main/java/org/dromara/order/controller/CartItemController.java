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
import org.dromara.order.domain.vo.CartItemVo;
import org.dromara.order.domain.bo.CartItemBo;
import org.dromara.order.service.ICartItemService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 购物车明细
 * 前端访问路由地址为:/order/cartItem
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cartItem")
public class CartItemController extends BaseController {

    private final ICartItemService cartItemService;

    /**
     * 查询购物车明细列表
     */
    @SaCheckPermission("order:cartItem:list")
    @GetMapping("/list")
    public TableDataInfo<CartItemVo> list(CartItemBo bo, PageQuery pageQuery) {
        return cartItemService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出购物车明细列表
     */
    @SaCheckPermission("order:cartItem:export")
    @Log(title = "购物车明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CartItemBo bo, HttpServletResponse response) {
        List<CartItemVo> list = cartItemService.queryList(bo);
        ExcelUtil.exportExcel(list, "购物车明细", CartItemVo.class, response);
    }

    /**
     * 获取购物车明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:cartItem:query")
    @GetMapping("/{id}")
    public R<CartItemVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(cartItemService.queryById(id));
    }

    /**
     * 新增购物车明细
     */
    @SaCheckPermission("order:cartItem:add")
    @Log(title = "购物车明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CartItemBo bo) {
        return toAjax(cartItemService.insertByBo(bo));
    }

    /**
     * 修改购物车明细
     */
    @SaCheckPermission("order:cartItem:edit")
    @Log(title = "购物车明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CartItemBo bo) {
        return toAjax(cartItemService.updateByBo(bo));
    }

    /**
     * 删除购物车明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:cartItem:remove")
    @Log(title = "购物车明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(cartItemService.deleteWithValidByIds(List.of(ids), true));
    }
}
