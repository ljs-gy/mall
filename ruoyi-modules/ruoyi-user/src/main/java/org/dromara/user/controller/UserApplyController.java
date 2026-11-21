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
import org.dromara.user.domain.vo.UserApplyVo;
import org.dromara.user.domain.bo.UserApplyAuditBo;
import org.dromara.user.domain.bo.UserApplyBo;
import org.dromara.user.service.IUserApplyService;
import org.dromara.common.mybatis.core.page.TableDataInfo;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userApply")
public class UserApplyController extends BaseController {

    private final IUserApplyService userApplyService;

    /**
     * 查询用户申请审核记录列表
     */
    @SaCheckPermission("user:userApply:list")
    @GetMapping("/list")
    public TableDataInfo<UserApplyVo> list(UserApplyBo bo, PageQuery pageQuery) {
        return userApplyService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户申请审核记录列表
     */
    @SaCheckPermission("user:userApply:export")
    @Log(title = "用户申请审核记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserApplyBo bo, HttpServletResponse response) {
        List<UserApplyVo> list = userApplyService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户申请审核记录", UserApplyVo.class, response);
    }

    /**
     * 获取用户申请审核记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:userApply:query")
    @GetMapping("/{id}")
    public R<UserApplyVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userApplyService.queryById(id));
    }

    /**
     * 新增用户申请审核记录
     */
    @SaCheckPermission("user:userApply:add")
    @Log(title = "用户申请审核记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserApplyBo bo) {
        return toAjax(userApplyService.insertByBo(bo));
    }

    /**
     * 修改用户申请审核记录
     */
    @SaCheckPermission("user:userApply:edit")
    @Log(title = "用户申请审核记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserApplyBo bo) {
        return toAjax(userApplyService.updateByBo(bo));
    }


    @SaCheckPermission("user:userApply:edit")
    @Log(title = "用户申请审核", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/audit")
    public R<Void> audit(@Validated @RequestBody UserApplyAuditBo bo) {
        return toAjax(userApplyService.audit(bo));
    }

    /**
     * 删除用户申请审核记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:userApply:remove")
    @Log(title = "用户申请审核记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userApplyService.deleteWithValidByIds(List.of(ids), true));
    }
}
