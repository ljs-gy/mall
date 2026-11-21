package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算确认页 - 店铺商品分组
 */
@Data
public class PcCheckoutMerchantGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 店铺代码（member_code） */
    private String memberCode;
    /** 店铺名称 */
    private String memberName;
    /** 本店商品清单（店促选中/赠品在行上 selectedShopActivityId、giftList） */
    private List<PcCheckoutProductVo> productList;
    /** 本店仅命中一个店促活动包时回填；多包时为 null */
    private Long selectedActivityId;
    /** 选中店内活动类型（含 FULL_GIFT） */
    private String selectedPromoType;
    /** 选中店内活动标题 */
    private String selectedActivityTitle;
    /** 本店商品小计（改价后行小计之和，未扣任何优惠） */
    private BigDecimal shopTotalAmount;
    /** 单店满减/满折优惠减免金额（按本店原价门槛判断） */
    private BigDecimal shopPromoDiscount;
    /** 店铺承担的跨店满减分摊额（按各店合资格商品小计占比分摊；合资格为 0 则不分摊） */
    private BigDecimal shopCrossReduceDiscount;
    /** 店铺承担的优惠券分摊额（按各店券适用行扣完店内+跨店后的剩余占比分摊） */
    private BigDecimal shopCouponDiscount;
    /**
     * 本店运费（元）。
     * 规划：按本店各商品运费模板计算后汇总；当前未接模板，占位为 0。
     */
    private BigDecimal shopFreightAmount;
    /** 本店商品应付（小计 - 单店优惠 - 跨店分摊 - 券分摊，不含运费） */
    private BigDecimal shopPayAmount;
}
