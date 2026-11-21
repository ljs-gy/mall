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
import org.dromara.user.domain.vo.GroupempVo;
import org.dromara.user.domain.bo.GroupempBo;
import org.dromara.user.service.IGroupempService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 群组员工职位关系
 * 前端访问路由地址为:/user/groupemp
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/groupemp")
public class GroupempController extends BaseController {

    private final IGroupempService groupempService;

    /**
     * 查询群组员工职位关系列表
     */
    @SaCheckPermission("user:groupemp:list")
    @GetMapping("/list")
    public TableDataInfo<GroupempVo> list(GroupempBo bo, PageQuery pageQuery) {
        return groupempService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出群组员工职位关系列表
     */
    @SaCheckPermission("user:groupemp:export")
    @Log(title = "群组员工职位关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(GroupempBo bo, HttpServletResponse response) {
        List<GroupempVo> list = groupempService.queryList(bo);
        ExcelUtil.exportExcel(list, "群组员工职位关系", GroupempVo.class, response);
    }

    /**
     * 获取群组员工职位关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:groupemp:query")
    @GetMapping("/{id}")
    public R<GroupempVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(groupempService.queryById(id));
    }

    /**
     * 新增群组员工职位关系
     */
    @SaCheckPermission("user:groupemp:add")
    @Log(title = "群组员工职位关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody GroupempBo bo) {
        return toAjax(groupempService.insertByBo(bo));
    }

    /**
     * 修改群组员工职位关系
     */
    @SaCheckPermission("user:groupemp:edit")
    @Log(title = "群组员工职位关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody GroupempBo bo) {
        return toAjax(groupempService.updateByBo(bo));
    }

    /**
     * 删除群组员工职位关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:groupemp:remove")
    @Log(title = "群组员工职位关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(groupempService.deleteWithValidByIds(List.of(ids), true));
    }
}
