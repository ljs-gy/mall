package org.dromara.pm.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class PcPdpMarketingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 当前商品可领/可用的优惠券模板
    private List<CouponTemplateVo> couponList = new ArrayList<>();

    // 当前商品能参与的促销活动
    private List<PcPromoActivityVo> promoList = new ArrayList<>();

    // 满赠发券规则
    private List<CouponGiftGrantRuleVo> giftGrantList = new ArrayList<>();
}
