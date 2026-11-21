package org.dromara.dis.controller;

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
import org.dromara.dis.domain.vo.DistProductCommissionVo;
import org.dromara.dis.domain.bo.DistProductCommissionBo;
import org.dromara.dis.service.IDistProductCommissionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品分佣配置
 * 前端访问路由地址为:/dis/productCommission
 *
 * @author ljs
 * @date 2026-08-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/productCommission")
public class DistProductCommissionController extends BaseController {

    private final IDistProductCommissionService distProductCommissionService;

    /**
     * 查询商品分佣配置列表
     */
    @SaCheckPermission("dis:productCommission:list")
    @GetMapping("/list")
    public TableDataInfo<DistProductCommissionVo> list(DistProductCommissionBo bo, PageQuery pageQuery) {
        return distProductCommissionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品分佣配置列表
     */
    @SaCheckPermission("dis:productCommission:export")
    @Log(title = "商品分佣配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DistProductCommissionBo bo, HttpServletResponse response) {
        List<DistProductCommissionVo> list = distProductCommissionService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品分佣配置", DistProductCommissionVo.class, response);
    }

    /**
     * 获取商品分佣配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:productCommission:query")
    @GetMapping("/{id}")
    public R<DistProductCommissionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(distProductCommissionService.queryById(id));
    }

    /**
     * 新增商品分佣配置
     */
    @SaCheckPermission("dis:productCommission:add")
    @Log(title = "商品分佣配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DistProductCommissionBo bo) {
        return toAjax(distProductCommissionService.insertByBo(bo));
    }

    /**
     * 修改商品分佣配置
     */
    @SaCheckPermission("dis:productCommission:edit")
    @Log(title = "商品分佣配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DistProductCommissionBo bo) {
        return toAjax(distProductCommissionService.updateByBo(bo));
    }

    /**
     * 删除商品分佣配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:productCommission:remove")
    @Log(title = "商品分佣配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(distProductCommissionService.deleteWithValidByIds(List.of(ids), true));
    }
}
