package org.dromara.bidding.controller;

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
import org.dromara.bidding.domain.vo.AnnouncementItemGoodsVo;
import org.dromara.bidding.domain.bo.AnnouncementItemGoodsBo;
import org.dromara.bidding.service.IAnnouncementItemGoodsService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 包裹子商品
 * 前端访问路由地址为:/bidding/announcementItemGoods
 *
 * @author ljs
 * @date 2026-06-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/announcementItemGoods")
public class AnnouncementItemGoodsController extends BaseController {

    private final IAnnouncementItemGoodsService announcementItemGoodsService;

    @SaCheckPermission("bidding:announcementItemGoods:list")
    @GetMapping("/list")
    public TableDataInfo<AnnouncementItemGoodsVo> list(AnnouncementItemGoodsBo bo, PageQuery pageQuery) {
        return announcementItemGoodsService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("bidding:announcementItemGoods:export")
    @Log(title = "包裹子商品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AnnouncementItemGoodsBo bo, HttpServletResponse response) {
        List<AnnouncementItemGoodsVo> list = announcementItemGoodsService.queryList(bo);
        ExcelUtil.exportExcel(list, "包裹子商品", AnnouncementItemGoodsVo.class, response);
    }

    @SaCheckPermission("bidding:announcementItemGoods:query")
    @GetMapping("/{id}")
    public R<AnnouncementItemGoodsVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(announcementItemGoodsService.queryById(id));
    }

    @SaCheckPermission("bidding:announcementItemGoods:add")
    @Log(title = "包裹子商品", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AnnouncementItemGoodsBo bo) {
        return toAjax(announcementItemGoodsService.insertByBo(bo));
    }

    @SaCheckPermission("bidding:announcementItemGoods:edit")
    @Log(title = "包裹子商品", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AnnouncementItemGoodsBo bo) {
        return toAjax(announcementItemGoodsService.updateByBo(bo));
    }

    @SaCheckPermission("bidding:announcementItemGoods:remove")
    @Log(title = "包裹子商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(announcementItemGoodsService.deleteWithValidByIds(List.of(ids), true));
    }
}
