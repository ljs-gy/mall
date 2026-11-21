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
import org.dromara.goods.domain.vo.ImageVo;
import org.dromara.goods.domain.bo.ImageBo;
import org.dromara.goods.service.IImageService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商品图片明细
 * 前端访问路由地址为:/goods/image
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/image")
public class ImageController extends BaseController {

    private final IImageService imageService;

    /**
     * 查询商品图片明细列表
     */
    @SaCheckPermission("goods:image:list")
    @GetMapping("/list")
    public TableDataInfo<ImageVo> list(ImageBo bo, PageQuery pageQuery) {
        return imageService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商品图片明细列表
     */
    @SaCheckPermission("goods:image:export")
    @Log(title = "商品图片明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ImageBo bo, HttpServletResponse response) {
        List<ImageVo> list = imageService.queryList(bo);
        ExcelUtil.exportExcel(list, "商品图片明细", ImageVo.class, response);
    }

    /**
     * 获取商品图片明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("goods:image:query")
    @GetMapping("/{id}")
    public R<ImageVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(imageService.queryById(id));
    }

    /**
     * 新增商品图片明细
     */
    @SaCheckPermission("goods:image:add")
    @Log(title = "商品图片明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ImageBo bo) {
        return toAjax(imageService.insertByBo(bo));
    }

    /**
     * 修改商品图片明细
     */
    @SaCheckPermission("goods:image:edit")
    @Log(title = "商品图片明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ImageBo bo) {
        return toAjax(imageService.updateByBo(bo));
    }

    /**
     * 删除商品图片明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("goods:image:remove")
    @Log(title = "商品图片明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(imageService.deleteWithValidByIds(List.of(ids), true));
    }
}
