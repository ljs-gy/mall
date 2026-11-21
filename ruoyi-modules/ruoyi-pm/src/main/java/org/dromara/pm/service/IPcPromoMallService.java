package org.dromara.pm.service;

import org.dromara.common.mybatis.core.page.TableDataInfo;
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


public interface IPcPromoMallService {


    TableDataInfo<PcPromoActivityVo> queryPcPromoActivityList(PcPromoQueryBo query);


    TableDataInfo<PcPromoGoodsVo> queryPromoGoodsList(PcPromoGoodsQueryBo query);


    TableDataInfo<PcSeckillZoneGoodsVo> querySeckillZoneGoodsList(PcSeckillZoneQueryBo query);


    PcPdpMarketingVo queryPdpMarketingInfo(PcPdpMarketingQueryBo query);


    TableDataInfo<CouponTemplateVo> queryCouponCenterList(PcCouponCenterQueryBo query);


    TableDataInfo<CouponUserRelVo> queryMyCouponList(PcMyCouponQueryBo query);


    void claimCoupon(PcCouponClaimBo bo);
}
