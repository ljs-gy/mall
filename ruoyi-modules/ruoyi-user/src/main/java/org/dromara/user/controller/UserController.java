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
import org.dromara.user.domain.vo.UserVo;
import org.dromara.user.domain.bo.UserBo;
import org.dromara.user.service.IUserService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户
 * 前端访问路由地址为:/user/user
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private final IUserService userService;

    /**
     * 查询用户列表
     */
    @SaCheckPermission("user:user:list")
    @GetMapping("/list")
    public TableDataInfo<UserVo> list(UserBo bo, PageQuery pageQuery) {
        return userService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户列表
     */
    @SaCheckPermission("user:user:export")
    @Log(title = "用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserBo bo, HttpServletResponse response) {
        List<UserVo> list = userService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户", UserVo.class, response);
    }

    /**
     * 获取用户详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:user:query")
    @GetMapping("/{id}")
    public R<UserVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userService.queryById(id));
    }

    /**
     * 新增用户
     */
    @SaCheckPermission("user:user:add")
    @Log(title = "用户", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserBo bo) {
        return toAjax(userService.insertByBo(bo));
    }

    /**
     * 修改用户
     */
    @SaCheckPermission("user:user:edit")
    @Log(title = "用户", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserBo bo) {
        return toAjax(userService.updateByBo(bo));
    }

    /**
     * 删除用户
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:user:remove")
    @Log(title = "用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userService.deleteWithValidByIds(List.of(ids), true));
    }
}
