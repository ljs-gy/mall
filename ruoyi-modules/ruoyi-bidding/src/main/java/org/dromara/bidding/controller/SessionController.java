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
import org.dromara.bidding.domain.vo.SessionVo;
import org.dromara.bidding.domain.bo.SessionBo;
import org.dromara.bidding.service.ISessionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 竞价场次
 * 前端访问路由地址为:/bidding/session
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/session")
public class SessionController extends BaseController {

    private final ISessionService sessionService;

    @SaCheckPermission("bidding:session:list")
    @GetMapping("/list")
    public TableDataInfo<SessionVo> list(SessionBo bo, PageQuery pageQuery) {
        return sessionService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:session:export")
    @Log(title = "竞价场次", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SessionBo bo, HttpServletResponse response) {
        List<SessionVo> list = sessionService.queryList(bo);
        ExcelUtil.exportExcel(list, "竞价场次", SessionVo.class, response);
    }

    @SaCheckPermission("bidding:session:query")
    @GetMapping("/{id}")
    public R<SessionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(sessionService.queryById(id));
    }

    @SaCheckPermission("bidding:session:add")
    @Log(title = "竞价场次", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SessionBo bo) {
        return toAjax(sessionService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:session:edit")
    @Log(title = "竞价场次", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SessionBo bo) {
        return toAjax(sessionService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:session:approve")
    @Log(title = "竞价场次", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/approve/{id}")
    public R<Void> approve(@NotNull(message = "主键不能为空")
                           @PathVariable("id") Long id) {
        sessionService.approveAnnouncement(id);
        return R.ok();
    }

    @SaCheckPermission("bidding:session:remove")
    @Log(title = "竞价场次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(sessionService.deleteWithValidByIds(List.of(ids), true));
    }
}
