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
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.bo.AfterSaleBo;
import org.dromara.order.service.IAfterSaleService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 售后退货单
 * 前端访问路由地址为:/order/afterSale
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/afterSale")
public class AfterSaleController extends BaseController {

    private final IAfterSaleService afterSaleService;

    /**
     * 查询售后退货单列表
     */
    @SaCheckPermission("order:afterSale:list")
    @GetMapping("/list")
    public TableDataInfo<AfterSaleVo> list(AfterSaleBo bo, PageQuery pageQuery) {
        return afterSaleService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出售后退货单列表
     */
    @SaCheckPermission("order:afterSale:export")
    @Log(title = "售后退货单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AfterSaleBo bo, HttpServletResponse response) {
        List<AfterSaleVo> list = afterSaleService.queryList(bo);
        ExcelUtil.exportExcel(list, "售后退货单", AfterSaleVo.class, response);
    }

    /**
     * 获取售后退货单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:afterSale:query")
    @GetMapping("/{id}")
    public R<AfterSaleVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(afterSaleService.queryById(id));
    }

    /**
     * 新增售后退货单
     */
    @SaCheckPermission("order:afterSale:add")
    @Log(title = "售后退货单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AfterSaleBo bo) {
        return toAjax(afterSaleService.insertByBo(bo));
    }

    /**
     * 修改售后退货单
     */
    @SaCheckPermission("order:afterSale:edit")
    @Log(title = "售后退货单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AfterSaleBo bo) {
        return toAjax(afterSaleService.updateByBo(bo));
    }

    /**
     * 删除售后退货单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:afterSale:remove")
    @Log(title = "售后退货单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(afterSaleService.deleteWithValidByIds(List.of(ids), true));
    }
}
