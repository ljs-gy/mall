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
import org.dromara.user.domain.vo.StoreVo;
import org.dromara.user.domain.bo.StoreBo;
import org.dromara.user.service.IStoreService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商家店铺
 * 前端访问路由地址为:/user/store
 *
 * @author ljs
 * @date 2026-07-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/store")
public class StoreController extends BaseController {

    private final IStoreService storeService;

    /**
     * 查询商家店铺列表
     */
    @SaCheckPermission("user:store:list")
    @GetMapping("/list")
    public TableDataInfo<StoreVo> list(StoreBo bo, PageQuery pageQuery) {
        return storeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商家店铺列表
     */
    @SaCheckPermission("user:store:export")
    @Log(title = "商家店铺", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(StoreBo bo, HttpServletResponse response) {
        List<StoreVo> list = storeService.queryList(bo);
        ExcelUtil.exportExcel(list, "商家店铺", StoreVo.class, response);
    }

    /**
     * 获取商家店铺详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:store:query")
    @GetMapping("/{id}")
    public R<StoreVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(storeService.queryById(id));
    }

    /**
     * 新增商家店铺
     */
    @SaCheckPermission("user:store:add")
    @Log(title = "商家店铺", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody StoreBo bo) {
        return toAjax(storeService.insertByBo(bo));
    }

    /**
     * 修改商家店铺
     */
    @SaCheckPermission("user:store:edit")
    @Log(title = "商家店铺", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody StoreBo bo) {
        return toAjax(storeService.updateByBo(bo));
    }

    /**
     * 删除商家店铺
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:store:remove")
    @Log(title = "商家店铺", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(storeService.deleteWithValidByIds(List.of(ids), true));
    }
}
