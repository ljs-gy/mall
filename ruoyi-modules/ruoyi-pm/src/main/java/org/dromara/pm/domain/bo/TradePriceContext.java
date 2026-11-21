package org.dromara.pm.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 下单算价上下文（供后续 submit 使用）
 */
@Data
public class TradePriceContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品原总额 */
    private BigDecimal totalProductAmount;
    /** 单店优惠总额 */
    private BigDecimal totalShopPromoDiscount;
    /** 跨店满减总额 */
    private BigDecimal totalCrossReduceDiscount;
    /** 优惠券抵扣总额 */
    private BigDecimal totalCouponDiscount;
    /** 运费总额 */
    private BigDecimal totalFreightAmount;
    /** 最终实付总金额 */
    private BigDecimal finalPayAmount;

    /** 最终核准生效的优惠券记录ID (pm_coupon_user_rel.id) */
    private Long selectedUserCouponId;
    /** 优惠券模板ID */
    private Long couponTemplateId;
    /** 优惠券标题 */
    private String couponTitle;

    /** 各店铺算价明细列表 */
    private List<ShopPriceGroup> shopGroups;

    @Data
    public static class ShopPriceGroup implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String memberCode;
        private String memberName;
        private Long selectedActivityId;
        private String promoType;
        private String activityTitle;
        /** 满赠礼品名称 */
        private String giftName;
        private BigDecimal shopTotalAmount;
        private BigDecimal shopPromoDiscount;
        private BigDecimal shopCrossReduceDiscount;
        private BigDecimal shopCouponDiscount;
        private BigDecimal shopPayAmount;
        private List<SkuPriceItem> skuList;
    }

    @Data
    public static class SkuPriceItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long spuId;
        private Long skuId;
        private String skuCode;
        private String goodsTitle;
        private String specText;
        private String coverImage;
        private String memberCcode;
        private List<Long> categoryAncestorIds;
        private Long brandId;
        private Integer buyCount;
        private BigDecimal originalPrice;
        private BigDecimal actualUnitPrice;
        private BigDecimal subTotalAmount;
        private BigDecimal itemShopDiscount;
        private BigDecimal itemCrossDiscount;
        private BigDecimal itemCouponDiscount;
        private BigDecimal itemRealPay;
    }
}
