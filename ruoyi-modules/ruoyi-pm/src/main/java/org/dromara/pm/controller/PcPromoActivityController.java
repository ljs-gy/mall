package org.dromara.pm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.pm.domain.bo.PcCouponCenterQueryBo;
import org.dromara.pm.domain.bo.PcCouponClaimBo;
import org.dromara.pm.domain.bo.PcMyCouponQueryBo;
import org.dromara.pm.domain.bo.PcPdpMarketingQueryBo;
import org.dromara.pm.domain.bo.PcPromoGoodsQueryBo;
import org.dromara.pm.domain.bo.PcPromoQueryBo;
import org.dromara.pm.domain.bo.PcSeckillZoneQueryBo;
import org.dromara.pm.domain.vo.CouponTemplateVo;
import org.dromara.pm.domain.vo.CouponUserRelVo;
import org.dromara.pm.domain.vo.PcPdpMarketingVo;
import org.dromara.pm.domain.vo.PcPromoActivityVo;
import org.dromara.pm.domain.vo.PcPromoGoodsVo;
import org.dromara.pm.domain.vo.PcSeckillZoneGoodsVo;
import org.dromara.pm.service.IPcPromoMallService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/promo")
public class PcPromoActivityController extends BaseController {

    private final IPcPromoMallService pcPromoMallService;

    /**
     * 活动列表
     */
    @GetMapping("/list")
    public TableDataInfo<PcPromoActivityVo> list(@Valid PcPromoQueryBo query) {
        return pcPromoMallService.queryPcPromoActivityList(query);
    }

    /**
     * 活动下商品列表
     */
    @GetMapping("/goods/list")
    public TableDataInfo<PcPromoGoodsVo> goodsList(@Valid PcPromoGoodsQueryBo query) {
        return pcPromoMallService.queryPromoGoodsList(query);
    }

    /**
     * 秒杀专区商品列表
     */
    @GetMapping("/seckill/goods/list")
    public TableDataInfo<PcSeckillZoneGoodsVo> seckillZoneGoodsList(@Valid PcSeckillZoneQueryBo query) {
        return pcPromoMallService.querySeckillZoneGoodsList(query);
    }

    /**
     * 商品详情页营销聚合
     */
    @GetMapping("/pdp/marketing")
    public R<PcPdpMarketingVo> pdpMarketing(@Valid PcPdpMarketingQueryBo query) {
        return R.ok(pcPromoMallService.queryPdpMarketingInfo(query));
    }

    /**
     * 领券中心列表
     */
    @GetMapping("/coupon/center/list")
    public TableDataInfo<CouponTemplateVo> couponCenterList(@Valid PcCouponCenterQueryBo query) {
        return pcPromoMallService.queryCouponCenterList(query);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/coupon/center/claim")
    public R<Void> couponClaim(@Valid @RequestBody PcCouponClaimBo bo) {
        pcPromoMallService.claimCoupon(bo);
        return R.ok();
    }

    /**
     * 我的优惠券
     */
    @GetMapping("/coupon/mine/list")
    public TableDataInfo<CouponUserRelVo> myCouponList(@Valid PcMyCouponQueryBo query) {
        return pcPromoMallService.queryMyCouponList(query);
    }
}
