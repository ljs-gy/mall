package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算确认页 - 费用汇总
 */
@Data
public class PcCheckoutPriceSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品总金额（各店商品小计之和） */
    private BigDecimal totalProductAmount;
    /** 单店促销优惠合计（满减/满折） */
    private BigDecimal totalShopPromoDiscount;
    /** 跨店满减/满折优惠合计 */
    private BigDecimal totalCrossReduceDiscount;
    /** 优惠券抵扣金额 */
    private BigDecimal totalCouponDiscount;
    /** 运费合计 */
    private BigDecimal totalFreightAmount;
    /** 最终应付金额 */
    private BigDecimal finalPayAmount;
    /** 命中的跨店活动 ID（提交扣预算用） */
    private Long selectedCrossActivityId;
    /** 命中的跨店活动类型 */
    private String selectedCrossPromoType;
    /** 命中的跨店活动标题 */
    private String selectedCrossActivityTitle;
    /** 命中跨店活动的平台分摊比例 */
    private BigDecimal selectedCrossPlatformShareRate;
    /** 命中跨店活动的商家分摊比例 */
    private BigDecimal selectedCrossMerchantShareRate;
}
