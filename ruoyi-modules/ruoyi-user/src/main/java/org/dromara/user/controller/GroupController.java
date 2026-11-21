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
import org.dromara.user.domain.vo.GroupVo;
import org.dromara.user.domain.bo.GroupBo;
import org.dromara.user.service.IGroupService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 群组
 * 前端访问路由地址为:/user/group
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupController extends BaseController {

    private final IGroupService groupService;

    /**
     * 查询群组列表
     */
    @SaCheckPermission("user:group:list")
    @GetMapping("/list")
    public TableDataInfo<GroupVo> list(GroupBo bo, PageQuery pageQuery) {
        return groupService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出群组列表
     */
    @SaCheckPermission("user:group:export")
    @Log(title = "群组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(GroupBo bo, HttpServletResponse response) {
        List<GroupVo> list = groupService.queryList(bo);
        ExcelUtil.exportExcel(list, "群组", GroupVo.class, response);
    }

    /**
     * 获取群组详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:group:query")
    @GetMapping("/{id}")
    public R<GroupVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(groupService.queryById(id));
    }

    /**
     * 新增群组
     */
    @SaCheckPermission("user:group:add")
    @Log(title = "群组", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody GroupBo bo) {
        return toAjax(groupService.insertByBo(bo));
    }

    /**
     * 修改群组
     */
    @SaCheckPermission("user:group:edit")
    @Log(title = "群组", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody GroupBo bo) {
        return toAjax(groupService.updateByBo(bo));
    }

    /**
     * 删除群组
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:group:remove")
    @Log(title = "群组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(groupService.deleteWithValidByIds(List.of(ids), true));
    }
}
