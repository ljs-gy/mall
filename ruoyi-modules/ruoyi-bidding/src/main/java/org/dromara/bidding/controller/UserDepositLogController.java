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
import org.dromara.bidding.domain.vo.UserDepositLogVo;
import org.dromara.bidding.domain.bo.UserDepositLogBo;
import org.dromara.bidding.service.IUserDepositLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 保证金流水
 * 前端访问路由地址为:/bidding/userDepositLog
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userDepositLog")
public class UserDepositLogController extends BaseController {

    private final IUserDepositLogService userDepositLogService;

    @SaCheckPermission("bidding:userDepositLog:list")
    @GetMapping("/list")
    public TableDataInfo<UserDepositLogVo> list(UserDepositLogBo bo, PageQuery pageQuery) {
        return userDepositLogService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:userDepositLog:export")
    @Log(title = "保证金流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserDepositLogBo bo, HttpServletResponse response) {
        List<UserDepositLogVo> list = userDepositLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "保证金流水", UserDepositLogVo.class, response);
    }

    @SaCheckPermission("bidding:userDepositLog:query")
    @GetMapping("/{id}")
    public R<UserDepositLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userDepositLogService.queryById(id));
    }

    @SaCheckPermission("bidding:userDepositLog:add")
    @Log(title = "保证金流水", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserDepositLogBo bo) {
        return toAjax(userDepositLogService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:userDepositLog:edit")
    @Log(title = "保证金流水", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserDepositLogBo bo) {
        return toAjax(userDepositLogService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:userDepositLog:remove")
    @Log(title = "保证金流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userDepositLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
