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
import org.dromara.pm.domain.vo.CouponUserRelVo;
import org.dromara.pm.domain.bo.CouponUserRelBo;
import org.dromara.pm.service.ICouponUserRelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户优惠券领用记录
 * 前端访问路由地址为:/pm/couponUserRel
 *
 * @author ljs
 * @date 2026-07-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/couponUserRel")
public class CouponUserRelController extends BaseController {

    private final ICouponUserRelService couponUserRelService;

    /**
     * 查询用户优惠券领用记录列表
     */
    @SaCheckPermission("pm:couponUserRel:list")
    @GetMapping("/list")
    public TableDataInfo<CouponUserRelVo> list(CouponUserRelBo bo, PageQuery pageQuery) {
        return couponUserRelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户优惠券领用记录列表
     */
    @SaCheckPermission("pm:couponUserRel:export")
    @Log(title = "用户优惠券领用记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CouponUserRelBo bo, HttpServletResponse response) {
        List<CouponUserRelVo> list = couponUserRelService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户优惠券领用记录", CouponUserRelVo.class, response);
    }

    /**
     * 获取用户优惠券领用记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:couponUserRel:query")
    @GetMapping("/{id}")
    public R<CouponUserRelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(couponUserRelService.queryById(id));
    }

    /**
     * 新增用户优惠券领用记录
     */
    @SaCheckPermission("pm:couponUserRel:add")
    @Log(title = "用户优惠券领用记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CouponUserRelBo bo) {
        return toAjax(couponUserRelService.insertByBo(bo));
    }

    /**
     * 修改用户优惠券领用记录
     */
    @SaCheckPermission("pm:couponUserRel:edit")
    @Log(title = "用户优惠券领用记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CouponUserRelBo bo) {
        return toAjax(couponUserRelService.updateByBo(bo));
    }

    /**
     * 删除用户优惠券领用记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:couponUserRel:remove")
    @Log(title = "用户优惠券领用记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(couponUserRelService.deleteWithValidByIds(List.of(ids), true));
    }
}
