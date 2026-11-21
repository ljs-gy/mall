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
import org.dromara.user.domain.vo.UserinfoVo;
import org.dromara.user.domain.bo.UserinfoBo;
import org.dromara.user.service.IUserinfoService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户信息
 * 前端访问路由地址为:/user/userinfo
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userinfo")
public class UserinfoController extends BaseController {

    private final IUserinfoService userinfoService;

    /**
     * 查询用户信息列表
     */
    @SaCheckPermission("user:userinfo:list")
    @GetMapping("/list")
    public TableDataInfo<UserinfoVo> list(UserinfoBo bo, PageQuery pageQuery) {
        return userinfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户信息列表
     */
    @SaCheckPermission("user:userinfo:export")
    @Log(title = "用户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserinfoBo bo, HttpServletResponse response) {
        List<UserinfoVo> list = userinfoService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户信息", UserinfoVo.class, response);
    }

    /**
     * 获取用户信息详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:userinfo:query")
    @GetMapping("/{id}")
    public R<UserinfoVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userinfoService.queryById(id));
    }

    /**
     * 新增用户信息
     */
    @SaCheckPermission("user:userinfo:add")
    @Log(title = "用户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserinfoBo bo) {
        return toAjax(userinfoService.insertByBo(bo));
    }

    /**
     * 修改用户信息
     */
    @SaCheckPermission("user:userinfo:edit")
    @Log(title = "用户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserinfoBo bo) {
        return toAjax(userinfoService.updateByBo(bo));
    }

    /**
     * 删除用户信息
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:userinfo:remove")
    @Log(title = "用户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userinfoService.deleteWithValidByIds(List.of(ids), true));
    }
}
