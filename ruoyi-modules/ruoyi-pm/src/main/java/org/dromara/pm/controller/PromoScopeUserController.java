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
import org.dromara.pm.domain.vo.PromoScopeUserVo;
import org.dromara.pm.domain.bo.PromoScopeUserBo;
import org.dromara.pm.service.IPromoScopeUserService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 营销活动与优惠券人群关系
 * 前端访问路由地址为:/pm/promoScopeUser
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promoScopeUser")
public class PromoScopeUserController extends BaseController {

    private final IPromoScopeUserService promoScopeUserService;

    /**
     * 查询营销活动与优惠券人群关系列表
     */
    @SaCheckPermission("pm:promoScopeUser:list")
    @GetMapping("/list")
    public TableDataInfo<PromoScopeUserVo> list(PromoScopeUserBo bo, PageQuery pageQuery) {
        return promoScopeUserService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出营销活动与优惠券人群关系列表
     */
    @SaCheckPermission("pm:promoScopeUser:export")
    @Log(title = "营销活动与优惠券人群关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PromoScopeUserBo bo, HttpServletResponse response) {
        List<PromoScopeUserVo> list = promoScopeUserService.queryList(bo);
        ExcelUtil.exportExcel(list, "营销活动与优惠券人群关系", PromoScopeUserVo.class, response);
    }

    /**
     * 获取营销活动与优惠券人群关系详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:promoScopeUser:query")
    @GetMapping("/{id}")
    public R<PromoScopeUserVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(promoScopeUserService.queryById(id));
    }

    /**
     * 新增营销活动与优惠券人群关系
     */
    @SaCheckPermission("pm:promoScopeUser:add")
    @Log(title = "营销活动与优惠券人群关系", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PromoScopeUserBo bo) {
        return toAjax(promoScopeUserService.insertByBo(bo));
    }

    /**
     * 修改营销活动与优惠券人群关系
     */
    @SaCheckPermission("pm:promoScopeUser:edit")
    @Log(title = "营销活动与优惠券人群关系", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PromoScopeUserBo bo) {
        return toAjax(promoScopeUserService.updateByBo(bo));
    }

    /**
     * 删除营销活动与优惠券人群关系
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:promoScopeUser:remove")
    @Log(title = "营销活动与优惠券人群关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(promoScopeUserService.deleteWithValidByIds(List.of(ids), true));
    }
}
