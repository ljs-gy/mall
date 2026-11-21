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
import org.dromara.user.domain.vo.UserIdentityRelVo;
import org.dromara.user.domain.bo.UserIdentityRelBo;
import org.dromara.user.service.IUserIdentityRelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户主体身份关系
 * 前端访问路由地址为:/user/userIdentityRel
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userIdentityRel")
public class UserIdentityRelController extends BaseController {

    private final IUserIdentityRelService userIdentityRelService;

    /**
     * 查询用户主体身份关系列表
     */
    @SaCheckPermission("user:userIdentityRel:list")
    @GetMapping("/list")
    public TableDataInfo<UserIdentityRelVo> list(UserIdentityRelBo bo, PageQuery pageQuery) {
        return userIdentityRelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户主体身份关系列表
     */
    @SaCheckPermission("user:userIdentityRel:export")
    @Log(title = "用户主体身份关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserIdentityRelBo bo, HttpServletResponse response) {
        List<UserIdentityRelVo> list = userIdentityRelService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户主体身份关系", UserIdentityRelVo.class, response);
    }

    /**
     * 获取用户主体身份关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:userIdentityRel:query")
    @GetMapping("/{id}")
    public R<UserIdentityRelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userIdentityRelService.queryById(id));
    }

    /**
     * 新增用户主体身份关系
     */
    @SaCheckPermission("user:userIdentityRel:add")
    @Log(title = "用户主体身份关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserIdentityRelBo bo) {
        return toAjax(userIdentityRelService.insertByBo(bo));
    }

    /**
     * 修改用户主体身份关系
     */
    @SaCheckPermission("user:userIdentityRel:edit")
    @Log(title = "用户主体身份关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserIdentityRelBo bo) {
        return toAjax(userIdentityRelService.updateByBo(bo));
    }

    /**
     * 删除用户主体身份关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:userIdentityRel:remove")
    @Log(title = "用户主体身份关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userIdentityRelService.deleteWithValidByIds(List.of(ids), true));
    }
}
