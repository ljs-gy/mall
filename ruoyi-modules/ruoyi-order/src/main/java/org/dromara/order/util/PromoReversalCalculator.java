package org.dromara.order.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.order.domain.OrderPromoItem;
import org.dromara.order.domain.bo.ReverseAgg;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * 专门算这次冲销该冲几件、冲多少钱，不写库，只出计算结果。
 */
public final class PromoReversalCalculator {

    private PromoReversalCalculator() {
    }

    @Getter
    @AllArgsConstructor
    public static class ReversalCalculation {
        // 本次冲正件数
        private final int quantity;
        // 本次冲掉的优惠金额
        private final BigDecimal discount;
        // 本次冲掉的平台承担金额
        private final BigDecimal platform;
        //本次冲掉的商家承担金额
        private final BigDecimal merchant;
    }

    /**
     * 按这次冲几件算出优惠 / 平台 / 商家各冲多少钱。
     * @param refundQty 本次申请冲正件数
     * @param remainQty 该正向流水剩余可冲件数
     * @param buyQty    正向下单件数
     */
    public static ReversalCalculation calculate(OrderPromoItem fwd, ReverseAgg done, int refundQty, int remainQty, int buyQty) {
        boolean isLast = refundQty == remainQty;
        BigDecimal disc;
        BigDecimal platform;
        BigDecimal merchant;
        if (isLast) {
            // 最后一笔：正向总额 − 已冲金额
            disc = scale(fwd.getDiscountAmount().subtract(done.getDiscount()));
            platform = scale(fwd.getPlatformShareAmount().subtract(done.getPlatform()));
            merchant = scale(fwd.getMerchantShareAmount().subtract(done.getMerchant()));
        } else {
            // 正向总额 ÷ 买件数 × 本次件数
            disc = scale(floorUnit(fwd.getDiscountAmount(), buyQty).multiply(BigDecimal.valueOf(refundQty)));
            platform = scale(floorUnit(fwd.getPlatformShareAmount(), buyQty).multiply(BigDecimal.valueOf(refundQty)));
            merchant = scale(floorUnit(fwd.getMerchantShareAmount(), buyQty).multiply(BigDecimal.valueOf(refundQty)));
        }
        return new ReversalCalculation(refundQty, disc, platform, merchant);
    }

    private static BigDecimal floorUnit(BigDecimal total, int qty) {
        return total.divide(BigDecimal.valueOf(qty), 2, RoundingMode.DOWN);
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
