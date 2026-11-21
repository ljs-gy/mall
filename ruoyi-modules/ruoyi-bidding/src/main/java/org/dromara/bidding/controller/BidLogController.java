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
import org.dromara.bidding.domain.vo.BidLogVo;
import org.dromara.bidding.domain.bo.BidLogBo;
import org.dromara.bidding.domain.bo.DoBidBo;
import org.dromara.bidding.service.IBidLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 出价流水
 * 前端访问路由地址为:/bidding/bidLog
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/bidLog")
public class BidLogController extends BaseController {

    private final IBidLogService bidLogService;

    @SaCheckPermission("bidding:bidLog:list")
    @GetMapping("/list")
    public TableDataInfo<BidLogVo> list(BidLogBo bo, PageQuery pageQuery) {
        return bidLogService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:bidLog:export")
    @Log(title = "出价流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BidLogBo bo, HttpServletResponse response) {
        List<BidLogVo> list = bidLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "出价流水", BidLogVo.class, response);
    }

    @SaCheckPermission("bidding:bidLog:query")
    @GetMapping("/{id}")
    public R<BidLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(bidLogService.queryById(id));
    }

    @SaCheckPermission("bidding:bidLog:add")
    @Log(title = "出价流水", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BidLogBo bo) {
        return toAjax(bidLogService.insertByBo(bo));
    }

    @Log(title = "买家出价", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/doBid")
    public R<Void> doBid(@Validated @RequestBody DoBidBo bo) {
        bidLogService.doBidAdvanced(bo);
        return R.ok();
    }

    @SaCheckPermission("bidding:bidLog:edit")
    @Log(title = "出价流水", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BidLogBo bo) {
        return toAjax(bidLogService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:bidLog:remove")
    @Log(title = "出价流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(bidLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
