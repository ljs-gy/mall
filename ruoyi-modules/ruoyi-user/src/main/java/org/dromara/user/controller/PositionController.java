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
import org.dromara.user.domain.vo.PositionVo;
import org.dromara.user.domain.bo.PositionBo;
import org.dromara.user.service.IPositionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 职位
 * 前端访问路由地址为:/user/position
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/position")
public class PositionController extends BaseController {

    private final IPositionService positionService;

    /**
     * 查询职位列表
     */
    @SaCheckPermission("user:position:list")
    @GetMapping("/list")
    public TableDataInfo<PositionVo> list(PositionBo bo, PageQuery pageQuery) {
        return positionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出职位列表
     */
    @SaCheckPermission("user:position:export")
    @Log(title = "职位", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PositionBo bo, HttpServletResponse response) {
        List<PositionVo> list = positionService.queryList(bo);
        ExcelUtil.exportExcel(list, "职位", PositionVo.class, response);
    }

    /**
     * 获取职位详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:position:query")
    @GetMapping("/{id}")
    public R<PositionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(positionService.queryById(id));
    }

    /**
     * 新增职位
     */
    @SaCheckPermission("user:position:add")
    @Log(title = "职位", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PositionBo bo) {
        return toAjax(positionService.insertByBo(bo));
    }

    /**
     * 修改职位
     */
    @SaCheckPermission("user:position:edit")
    @Log(title = "职位", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PositionBo bo) {
        return toAjax(positionService.updateByBo(bo));
    }

    /**
     * 删除职位
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:position:remove")
    @Log(title = "职位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(positionService.deleteWithValidByIds(List.of(ids), true));
    }
}
