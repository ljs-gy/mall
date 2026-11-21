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
import org.dromara.order.domain.vo.AfterSaleItemVo;
import org.dromara.order.domain.bo.AfterSaleItemBo;
import org.dromara.order.service.IAfterSaleItemService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 售后单明细
 * 前端访问路由地址为:/order/afterSaleItem
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/afterSaleItem")
public class AfterSaleItemController extends BaseController {

    private final IAfterSaleItemService afterSaleItemService;

    /**
     * 查询售后单明细列表
     */
    @SaCheckPermission("order:afterSaleItem:list")
    @GetMapping("/list")
    public TableDataInfo<AfterSaleItemVo> list(AfterSaleItemBo bo, PageQuery pageQuery) {
        return afterSaleItemService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出售后单明细列表
     */
    @SaCheckPermission("order:afterSaleItem:export")
    @Log(title = "售后单明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AfterSaleItemBo bo, HttpServletResponse response) {
        List<AfterSaleItemVo> list = afterSaleItemService.queryList(bo);
        ExcelUtil.exportExcel(list, "售后单明细", AfterSaleItemVo.class, response);
    }

    /**
     * 获取售后单明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:afterSaleItem:query")
    @GetMapping("/{id}")
    public R<AfterSaleItemVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(afterSaleItemService.queryById(id));
    }

    /**
     * 新增售后单明细
     */
    @SaCheckPermission("order:afterSaleItem:add")
    @Log(title = "售后单明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AfterSaleItemBo bo) {
        return toAjax(afterSaleItemService.insertByBo(bo));
    }

    /**
     * 修改售后单明细
     */
    @SaCheckPermission("order:afterSaleItem:edit")
    @Log(title = "售后单明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AfterSaleItemBo bo) {
        return toAjax(afterSaleItemService.updateByBo(bo));
    }

    /**
     * 删除售后单明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:afterSaleItem:remove")
    @Log(title = "售后单明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(afterSaleItemService.deleteWithValidByIds(List.of(ids), true));
    }
}
