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
import org.dromara.user.domain.vo.AccountFrozenVo;
import org.dromara.user.domain.bo.AccountFrozenBo;
import org.dromara.user.service.IAccountFrozenService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户资金账户冻结明细
 * 前端访问路由地址为:/user/accountFrozen
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/accountFrozen")
public class AccountFrozenController extends BaseController {

    private final IAccountFrozenService accountFrozenService;

    /**
     * 查询用户资金账户冻结明细列表
     */
    @SaCheckPermission("user:accountFrozen:list")
    @GetMapping("/list")
    public TableDataInfo<AccountFrozenVo> list(AccountFrozenBo bo, PageQuery pageQuery) {
        return accountFrozenService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户资金账户冻结明细列表
     */
    @SaCheckPermission("user:accountFrozen:export")
    @Log(title = "用户资金账户冻结明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AccountFrozenBo bo, HttpServletResponse response) {
        List<AccountFrozenVo> list = accountFrozenService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户资金账户冻结明细", AccountFrozenVo.class, response);
    }

    /**
     * 获取用户资金账户冻结明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:accountFrozen:query")
    @GetMapping("/{id}")
    public R<AccountFrozenVo> getInfo(@NotNull(message = "主键不能为空")
                                      @PathVariable("id") Long id) {
        return R.ok(accountFrozenService.queryById(id));
    }

    /**
     * 新增用户资金账户冻结明细
     */
    @SaCheckPermission("user:accountFrozen:add")
    @Log(title = "用户资金账户冻结明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AccountFrozenBo bo) {
        return toAjax(accountFrozenService.insertByBo(bo));
    }

    /**
     * 修改用户资金账户冻结明细
     */
    @SaCheckPermission("user:accountFrozen:edit")
    @Log(title = "用户资金账户冻结明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AccountFrozenBo bo) {
        return toAjax(accountFrozenService.updateByBo(bo));
    }

    /**
     * 删除用户资金账户冻结明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:accountFrozen:remove")
    @Log(title = "用户资金账户冻结明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(accountFrozenService.deleteWithValidByIds(List.of(ids), true));
    }

}
