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
import org.dromara.goods.domain.vo.SpuParameterValueVo;
import org.dromara.goods.domain.bo.SpuParameterValueBo;
import org.dromara.goods.service.ISpuParameterValueService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品SPU参数值
 * 前端访问路由地址为:/goods/spuParameterValue
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/spuParameterValue")
public class SpuParameterValueController extends BaseController {

    private final ISpuParameterValueService spuParameterValueService;

    /**
     * 查询商品SPU参数值列表
     */
    @SaCheckPermission("goods:spuParameterValue:list")
    @GetMapping("/list")
    public TableDataInfo<SpuParameterValueVo> list(SpuParameterValueBo bo, PageQuery pageQuery) {
        return spuParameterValueService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品SPU参数值列表
     */
    @SaCheckPermission("goods:spuParameterValue:export")
    @Log(title = "商品SPU参数值", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SpuParameterValueBo bo, HttpServletResponse response) {
        List<SpuParameterValueVo> list = spuParameterValueService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品SPU参数值", SpuParameterValueVo.class, response);
    }

    /**
     * 获取商品SPU参数值详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:spuParameterValue:query")
    @GetMapping("/{id}")
    public R<SpuParameterValueVo> getInfo(@NotNull(message = "主键不能为空")
                                          @PathVariable("id") Long id) {
        return R.ok(spuParameterValueService.queryById(id));
    }

    /**
     * 新增商品SPU参数值
     */
    @SaCheckPermission("goods:spuParameterValue:add")
    @Log(title = "商品SPU参数值", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpuParameterValueBo bo) {
        return toAjax(spuParameterValueService.insertByBo(bo));
    }

    /**
     * 修改商品SPU参数值
     */
    @SaCheckPermission("goods:spuParameterValue:edit")
    @Log(title = "商品SPU参数值", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpuParameterValueBo bo) {
        return toAjax(spuParameterValueService.updateByBo(bo));
    }

    /**
     * 删除商品SPU参数值
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:spuParameterValue:remove")
    @Log(title = "商品SPU参数值", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(spuParameterValueService.deleteWithValidByIds(List.of(ids), true));
    }
}
