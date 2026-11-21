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
import org.dromara.goods.domain.vo.ParameterVo;
import org.dromara.goods.domain.bo.ParameterBo;
import org.dromara.goods.service.IParameterService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品参数
 * 前端访问路由地址为:/goods/parameter
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/parameter")
public class ParameterController extends BaseController {

    private final IParameterService parameterService;

    /**
     * 查询商品参数列表
     */
    @SaCheckPermission("goods:parameter:list")
    @GetMapping("/list")
    public TableDataInfo<ParameterVo> list(ParameterBo bo, PageQuery pageQuery) {
        return parameterService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品参数列表
     */
    @SaCheckPermission("goods:parameter:export")
    @Log(title = "商品参数", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ParameterBo bo, HttpServletResponse response) {
        List<ParameterVo> list = parameterService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品参数", ParameterVo.class, response);
    }

    /**
     * 获取商品参数详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:parameter:query")
    @GetMapping("/{id}")
    public R<ParameterVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(parameterService.queryById(id));
    }

    /**
     * 新增商品参数
     */
    @SaCheckPermission("goods:parameter:add")
    @Log(title = "商品参数", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ParameterBo bo) {
        return toAjax(parameterService.insertByBo(bo));
    }

    /**
     * 修改商品参数
     */
    @SaCheckPermission("goods:parameter:edit")
    @Log(title = "商品参数", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ParameterBo bo) {
        return toAjax(parameterService.updateByBo(bo));
    }

    /**
     * 删除商品参数
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:parameter:remove")
    @Log(title = "商品参数", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(parameterService.deleteWithValidByIds(List.of(ids), true));
    }
}
