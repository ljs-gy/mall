package org.dromara.goods.controller;

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
import org.dromara.goods.domain.vo.SpecVo;
import org.dromara.goods.domain.bo.SpecBo;
import org.dromara.goods.service.ISpecService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品规格
 * 前端访问路由地址为:/goods/spec
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/spec")
public class SpecController extends BaseController {

    private final ISpecService specService;

    /**
     * 查询商品规格列表
     */
    @SaCheckPermission("goods:spec:list")
    @GetMapping("/list")
    public TableDataInfo<SpecVo> list(SpecBo bo, PageQuery pageQuery) {
        return specService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品规格列表
     */
    @SaCheckPermission("goods:spec:export")
    @Log(title = "商品规格", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SpecBo bo, HttpServletResponse response) {
        List<SpecVo> list = specService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品规格", SpecVo.class, response);
    }

    /**
     * 获取商品规格详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:spec:query")
    @GetMapping("/{id}")
    public R<SpecVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(specService.queryById(id));
    }

    /**
     * 新增商品规格
     */
    @SaCheckPermission("goods:spec:add")
    @Log(title = "商品规格", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpecBo bo) {
        return toAjax(specService.insertByBo(bo));
    }

    /**
     * 修改商品规格
     */
    @SaCheckPermission("goods:spec:edit")
    @Log(title = "商品规格", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecBo bo) {
        return toAjax(specService.updateByBo(bo));
    }

    /**
     * 删除商品规格
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:spec:remove")
    @Log(title = "商品规格", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(specService.deleteWithValidByIds(List.of(ids), true));
    }
}
