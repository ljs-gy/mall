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
import org.dromara.goods.domain.vo.SpecOptionVo;
import org.dromara.goods.domain.bo.SpecOptionBo;
import org.dromara.goods.service.ISpecOptionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品规格选项
 * 前端访问路由地址为:/goods/specOption
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/specOption")
public class SpecOptionController extends BaseController {

    private final ISpecOptionService specOptionService;

    /**
     * 查询商品规格选项列表
     */
    @SaCheckPermission("goods:specOption:list")
    @GetMapping("/list")
    public TableDataInfo<SpecOptionVo> list(SpecOptionBo bo, PageQuery pageQuery) {
        return specOptionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品规格选项列表
     */
    @SaCheckPermission("goods:specOption:export")
    @Log(title = "商品规格选项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SpecOptionBo bo, HttpServletResponse response) {
        List<SpecOptionVo> list = specOptionService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品规格选项", SpecOptionVo.class, response);
    }

    /**
     * 获取商品规格选项详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:specOption:query")
    @GetMapping("/{id}")
    public R<SpecOptionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(specOptionService.queryById(id));
    }

    /**
     * 新增商品规格选项
     */
    @SaCheckPermission("goods:specOption:add")
    @Log(title = "商品规格选项", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpecOptionBo bo) {
        return toAjax(specOptionService.insertByBo(bo));
    }

    /**
     * 修改商品规格选项
     */
    @SaCheckPermission("goods:specOption:edit")
    @Log(title = "商品规格选项", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecOptionBo bo) {
        return toAjax(specOptionService.updateByBo(bo));
    }

    /**
     * 删除商品规格选项
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:specOption:remove")
    @Log(title = "商品规格选项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(specOptionService.deleteWithValidByIds(List.of(ids), true));
    }
}
