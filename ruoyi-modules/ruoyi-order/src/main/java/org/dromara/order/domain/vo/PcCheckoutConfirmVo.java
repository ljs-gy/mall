package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 结算确认页响应视图
 */
@Data
public class PcCheckoutConfirmVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 收货地址信息 */
    private PcCheckoutAddressVo address;

    /** 店铺物权与商品清单分组 */
    private List<PcCheckoutMerchantGroupVo> merchantGroups;

    /** 优惠券选择器 */
    private PcCheckoutCouponSelectorVo couponSelector;

    /** 权威费用汇总明细 */
    private PcCheckoutPriceSummaryVo priceSummary;

    /** 满赠券发放奖励 */
    private List<PcCheckoutGiftGrantRewardVo> giftGrantRewards;
}
