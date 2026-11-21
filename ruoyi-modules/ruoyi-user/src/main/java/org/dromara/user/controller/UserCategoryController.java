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
import org.dromara.user.domain.vo.UserCategoryVo;
import org.dromara.user.domain.bo.UserCategoryBo;
import org.dromara.user.service.IUserCategoryService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户经营类目资质
 * 前端访问路由地址为:/user/userCategory
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userCategory")
public class UserCategoryController extends BaseController {

    private final IUserCategoryService userCategoryService;

    /**
     * 查询用户经营类目资质列表
     */
    @SaCheckPermission("user:userCategory:list")
    @GetMapping("/list")
    public TableDataInfo<UserCategoryVo> list(UserCategoryBo bo, PageQuery pageQuery) {
        return userCategoryService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户经营类目资质列表
     */
    @SaCheckPermission("user:userCategory:export")
    @Log(title = "用户经营类目资质", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserCategoryBo bo, HttpServletResponse response) {
        List<UserCategoryVo> list = userCategoryService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户经营类目资质", UserCategoryVo.class, response);
    }

    /**
     * 获取用户经营类目资质详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:userCategory:query")
    @GetMapping("/{id}")
    public R<UserCategoryVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userCategoryService.queryById(id));
    }

    /**
     * 新增用户经营类目资质
     */
    @SaCheckPermission("user:userCategory:add")
    @Log(title = "用户经营类目资质", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserCategoryBo bo) {
        return toAjax(userCategoryService.insertByBo(bo));
    }

    /**
     * 修改用户经营类目资质
     */
    @SaCheckPermission("user:userCategory:edit")
    @Log(title = "用户经营类目资质", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserCategoryBo bo) {
        return toAjax(userCategoryService.updateByBo(bo));
    }

    /**
     * 删除用户经营类目资质
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:userCategory:remove")
    @Log(title = "用户经营类目资质", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userCategoryService.deleteWithValidByIds(List.of(ids), true));
    }
}
