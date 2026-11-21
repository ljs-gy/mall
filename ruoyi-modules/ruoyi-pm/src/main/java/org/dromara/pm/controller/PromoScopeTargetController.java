package org.dromara.pm.controller;

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
import org.dromara.pm.domain.vo.PromoScopeTargetVo;
import org.dromara.pm.domain.bo.PromoScopeTargetBo;
import org.dromara.pm.service.IPromoScopeTargetService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 营销范围
 * 前端访问路由地址为:/pm/promoScopeTarget
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promoScopeTarget")
public class PromoScopeTargetController extends BaseController {

    private final IPromoScopeTargetService promoScopeTargetService;

    /**
     * 查询营销范围列表
     */
    @SaCheckPermission("pm:promoScopeTarget:list")
    @GetMapping("/list")
    public TableDataInfo<PromoScopeTargetVo> list(PromoScopeTargetBo bo, PageQuery pageQuery) {
        return promoScopeTargetService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出营销范围列表
     */
    @SaCheckPermission("pm:promoScopeTarget:export")
    @Log(title = "营销范围", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PromoScopeTargetBo bo, HttpServletResponse response) {
        List<PromoScopeTargetVo> list = promoScopeTargetService.queryList(bo);
        ExcelUtil.exportExcel(list, "营销范围", PromoScopeTargetVo.class, response);
    }

    /**
     * 获取营销范围详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:promoScopeTarget:query")
    @GetMapping("/{id}")
    public R<PromoScopeTargetVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(promoScopeTargetService.queryById(id));
    }

    /**
     * 新增营销范围
     */
    @SaCheckPermission("pm:promoScopeTarget:add")
    @Log(title = "营销范围", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PromoScopeTargetBo bo) {
        return toAjax(promoScopeTargetService.insertByBo(bo));
    }

    /**
     * 修改营销范围
     */
    @SaCheckPermission("pm:promoScopeTarget:edit")
    @Log(title = "营销范围", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PromoScopeTargetBo bo) {
        return toAjax(promoScopeTargetService.updateByBo(bo));
    }

    /**
     * 删除营销范围
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:promoScopeTarget:remove")
    @Log(title = "营销范围", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(promoScopeTargetService.deleteWithValidByIds(List.of(ids), true));
    }
}
