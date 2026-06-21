package org.dromara.bidding.controller;

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
import org.dromara.bidding.domain.vo.BidOrderVo;
import org.dromara.bidding.domain.bo.BidOrderBo;
import org.dromara.bidding.service.IBidOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 竞价订单
 * 前端访问路由地址为:/bidding/order
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class BidOrderController extends BaseController {

    private final IBidOrderService bidOrderService;

    @SaCheckPermission("bidding:order:list")
    @GetMapping("/list")
    public TableDataInfo<BidOrderVo> list(BidOrderBo bo, PageQuery pageQuery) {
        return bidOrderService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:order:export")
    @Log(title = "竞价订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BidOrderBo bo, HttpServletResponse response) {
        List<BidOrderVo> list = bidOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "竞价订单", BidOrderVo.class, response);
    }

    @SaCheckPermission("bidding:order:query")
    @GetMapping("/{id}")
    public R<BidOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(bidOrderService.queryById(id));
    }

    @SaCheckPermission("bidding:order:add")
    @Log(title = "竞价订单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BidOrderBo bo) {
        return toAjax(bidOrderService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:order:edit")
    @Log(title = "竞价订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BidOrderBo bo) {
        return toAjax(bidOrderService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:order:remove")
    @Log(title = "竞价订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(bidOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
