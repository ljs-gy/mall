package org.dromara.user.controller;

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
import org.dromara.user.domain.vo.AccountVo;
import org.dromara.user.domain.bo.AccountBo;
import org.dromara.user.service.IAccountService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户资金账户
 * 前端访问路由地址为:/user/account
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/account")
public class AccountController extends BaseController {

    private final IAccountService accountService;

    /**
     * 查询用户资金账户列表
     */
    @SaCheckPermission("user:account:list")
    @GetMapping("/list")
    public TableDataInfo<AccountVo> list(AccountBo bo, PageQuery pageQuery) {
        return accountService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户资金账户列表
     */
    @SaCheckPermission("user:account:export")
    @Log(title = "用户资金账户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AccountBo bo, HttpServletResponse response) {
        List<AccountVo> list = accountService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户资金账户", AccountVo.class, response);
    }

    /**
     * 获取用户资金账户详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:account:query")
    @GetMapping("/{id}")
    public R<AccountVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(accountService.queryById(id));
    }

    /**
     * 新增用户资金账户
     */
    @SaCheckPermission("user:account:add")
    @Log(title = "用户资金账户", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AccountBo bo) {
        return toAjax(accountService.insertByBo(bo));
    }

    /**
     * 修改用户资金账户
     */
    @SaCheckPermission("user:account:edit")
    @Log(title = "用户资金账户", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AccountBo bo) {
        return toAjax(accountService.updateByBo(bo));
    }

    /**
     * 删除用户资金账户
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:account:remove")
    @Log(title = "用户资金账户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(accountService.deleteWithValidByIds(List.of(ids), true));
    }
}
