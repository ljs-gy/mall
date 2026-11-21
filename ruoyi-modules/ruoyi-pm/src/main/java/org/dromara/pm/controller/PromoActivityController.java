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
import org.dromara.pm.domain.vo.PromoActivityVo;
import org.dromara.pm.domain.bo.PromoActivityAuditBo;
import org.dromara.pm.domain.bo.PromoActivityBo;
import org.dromara.pm.domain.bo.ExclusivePromoActivityBo;
import org.dromara.pm.service.IPromoActivityService;
import org.dromara.common.mybatis.core.page.TableDataInfo;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promoActivity")
public class PromoActivityController extends BaseController {

    private final IPromoActivityService promoActivityService;

    /**
     * 查询营销活动主列表
     */
    @SaCheckPermission("pm:promoActivity:list")
    @GetMapping("/list")
    public TableDataInfo<PromoActivityVo> list(PromoActivityBo bo, PageQuery pageQuery) {
        return promoActivityService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出营销活动主列表
     */
    @SaCheckPermission("pm:promoActivity:export")
    @Log(title = "营销活动主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PromoActivityBo bo, HttpServletResponse response) {
        List<PromoActivityVo> list = promoActivityService.queryList(bo);
        ExcelUtil.exportExcel(list, "营销活动主", PromoActivityVo.class, response);
    }

    /**
     * 获取营销活动主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("pm:promoActivity:query")
    @GetMapping("/{id}")
    public R<PromoActivityVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(promoActivityService.queryById(id));
    }


    @SaCheckPermission("pm:promoActivity:add")
    @Log(title = "营销活动主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PromoActivityBo bo) {
        return toAjax(promoActivityService.insertByBo(bo));
    }

    /**
     * 新增满赠促销活动
     */
    @SaCheckPermission("pm:promoActivity:add")
    @Log(title = "满赠促销活动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/fullGift")
    public R<Void> addFullGift(@Validated(AddGroup.class) @RequestBody PromoActivityBo bo) {
        bo.setPromoType("FULL_GIFT");
        return toAjax(promoActivityService.insertByBo(bo));
    }

    /**
     * 新增秒杀活动
     */
    @SaCheckPermission("pm:promoActivity:add")
    @Log(title = "秒杀活动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/seckill")
    public R<Long> addSeckill(@Validated(AddGroup.class) @RequestBody ExclusivePromoActivityBo bo) {
        return R.ok(promoActivityService.createSeckillActivity(bo));
    }

    /**
     * 新增特价活动
     */
    @SaCheckPermission("pm:promoActivity:add")
    @Log(title = "特价活动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/specialPrice")
    public R<Long> addSpecialPrice(@Validated(AddGroup.class) @RequestBody ExclusivePromoActivityBo bo) {
        return R.ok(promoActivityService.createSpecialPriceActivity(bo));
    }

    /**
     * 新增跨店满减活动
     */
    @SaCheckPermission("pm:promoActivity:add")
    @Log(title = "跨店满减活动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/crossFullReduce")
    public R<Void> addCrossFullReduce(@Validated(AddGroup.class) @RequestBody PromoActivityBo bo) {
        bo.setPromoType("CROSS_FULL_REDUCE");
        return toAjax(promoActivityService.insertByBo(bo));
    }

    /**
     * 新增跨店满折活动
     */
    @SaCheckPermission("pm:promoActivity:add")
    @Log(title = "跨店满折活动", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/crossFullDiscount")
    public R<Void> addCrossFullDiscount(@Validated(AddGroup.class) @RequestBody PromoActivityBo bo) {
        bo.setPromoType("CROSS_FULL_DISCOUNT");
        return toAjax(promoActivityService.insertByBo(bo));
    }


    @SaCheckPermission("pm:promoActivity:audit")
    @Log(title = "促销活动审核", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/audit")
    public R<Void> audit(@Validated @RequestBody PromoActivityAuditBo bo) {
        promoActivityService.auditPromoActivity(bo);
        return R.ok();
    }

    /**
     * 修改营销活动主
     */
    @SaCheckPermission("pm:promoActivity:edit")
    @Log(title = "营销活动主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PromoActivityBo bo) {
        return toAjax(promoActivityService.updateByBo(bo));
    }

    /**
     * 删除营销活动主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("pm:promoActivity:remove")
    @Log(title = "营销活动主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(promoActivityService.deleteWithValidByIds(List.of(ids), true));
    }
}
