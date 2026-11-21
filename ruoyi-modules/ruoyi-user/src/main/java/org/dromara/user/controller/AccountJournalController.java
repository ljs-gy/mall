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
import org.dromara.user.domain.vo.AccountJournalVo;
import org.dromara.user.domain.bo.AccountJournalBo;
import org.dromara.user.service.IAccountJournalService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户账务动账流水
 * 前端访问路由地址为:/user/accountJournal
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/accountJournal")
public class AccountJournalController extends BaseController {

    private final IAccountJournalService accountJournalService;

    /**
     * 查询用户账务动账流水列表
     */
    @SaCheckPermission("user:accountJournal:list")
    @GetMapping("/list")
    public TableDataInfo<AccountJournalVo> list(AccountJournalBo bo, PageQuery pageQuery) {
        return accountJournalService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户账务动账流水列表
     */
    @SaCheckPermission("user:accountJournal:export")
    @Log(title = "用户账务动账流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AccountJournalBo bo, HttpServletResponse response) {
        List<AccountJournalVo> list = accountJournalService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户账务动账流水", AccountJournalVo.class, response);
    }

    /**
     * 获取用户账务动账流水详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:accountJournal:query")
    @GetMapping("/{id}")
    public R<AccountJournalVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(accountJournalService.queryById(id));
    }

    /**
     * 新增用户账务动账流水
     */
    @SaCheckPermission("user:accountJournal:add")
    @Log(title = "用户账务动账流水", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AccountJournalBo bo) {
        return toAjax(accountJournalService.insertByBo(bo));
    }

    /**
     * 修改用户账务动账流水
     */
    @SaCheckPermission("user:accountJournal:edit")
    @Log(title = "用户账务动账流水", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AccountJournalBo bo) {
        return toAjax(accountJournalService.updateByBo(bo));
    }

    /**
     * 删除用户账务动账流水
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:accountJournal:remove")
    @Log(title = "用户账务动账流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(accountJournalService.deleteWithValidByIds(List.of(ids), true));
    }
}
