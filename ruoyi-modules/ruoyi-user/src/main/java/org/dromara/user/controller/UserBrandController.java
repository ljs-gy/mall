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
import org.dromara.user.domain.vo.UserBrandVo;
import org.dromara.user.domain.bo.UserBrandBo;
import org.dromara.user.service.IUserBrandService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户经营品牌资质
 * 前端访问路由地址为:/user/userBrand
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/userBrand")
public class UserBrandController extends BaseController {

    private final IUserBrandService userBrandService;

    /**
     * 查询用户经营品牌资质列表
     */
    @SaCheckPermission("user:userBrand:list")
    @GetMapping("/list")
    public TableDataInfo<UserBrandVo> list(UserBrandBo bo, PageQuery pageQuery) {
        return userBrandService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户经营品牌资质列表
     */
    @SaCheckPermission("user:userBrand:export")
    @Log(title = "用户经营品牌资质", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(UserBrandBo bo, HttpServletResponse response) {
        List<UserBrandVo> list = userBrandService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户经营品牌资质", UserBrandVo.class, response);
    }

    /**
     * 获取用户经营品牌资质详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:userBrand:query")
    @GetMapping("/{id}")
    public R<UserBrandVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(userBrandService.queryById(id));
    }

    /**
     * 新增用户经营品牌资质
     */
    @SaCheckPermission("user:userBrand:add")
    @Log(title = "用户经营品牌资质", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody UserBrandBo bo) {
        return toAjax(userBrandService.insertByBo(bo));
    }

    /**
     * 修改用户经营品牌资质
     */
    @SaCheckPermission("user:userBrand:edit")
    @Log(title = "用户经营品牌资质", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody UserBrandBo bo) {
        return toAjax(userBrandService.updateByBo(bo));
    }

    /**
     * 删除用户经营品牌资质
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:userBrand:remove")
    @Log(title = "用户经营品牌资质", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(userBrandService.deleteWithValidByIds(List.of(ids), true));
    }
}
