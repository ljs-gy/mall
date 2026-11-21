package org.dromara.pm.utils;

import org.dromara.pm.enums.PromoMutexConstants;

/**
 * 营销叠加/互斥规则说明（算价流水线参考）
 * <pre>
 * 优先级（高→低）：EXCLUSIVE_PRICE(100) → SHOP_PROMO(80) → CROSS_PROMO(60) → 优惠券
 *
 * 1. 秒杀/特价（mutex_rule=2 绝对排他）：
 *    - 命中后阻断后续所有店铺/跨店活动与优惠券
 *    - 秒杀与特价同组互斥
 *
 * 2. 店内满减/满折/满赠（mutex_rule=1 同组择优）：
 *    - 三者互斥三选一，默认取优惠力度最大的一条
 *
 * 3. 跨店满减/满折（mutex_rule=1）：
 *    - 跨店组内互斥择优
 *    - 可与店内满减等叠加（不同组）
 *
 * 4. 优惠券：
 *    - 可与店内/跨店活动叠加，一单仅用一张
 *    - 不可与秒杀/特价叠加
 * </pre>
 */
public final class PromoStackRules {

    private PromoStackRules() {
    }

    public static boolean isAbsoluteExclusivePromo(String promoType) {
        return "SECKILL".equals(promoType) || "SPECIAL_PRICE".equals(promoType);
    }

    public static boolean isShopPromo(String promoType) {
        return "FULL_REDUCE".equals(promoType)
            || "FULL_DISCOUNT".equals(promoType)
            || "FULL_GIFT".equals(promoType);
    }

    public static boolean isCrossPromo(String promoType) {
        return "CROSS_FULL_REDUCE".equals(promoType)
            || "CROSS_FULL_DISCOUNT".equals(promoType);
    }

    /**
     * 命中绝对排他活动后，是否允许再叠加优惠券
     */
    public static boolean allowCouponWithPromo(String promoType) {
        return !isAbsoluteExclusivePromo(promoType);
    }

    public static String resolveDefaultGroupCode(String promoType) {
        if (isAbsoluteExclusivePromo(promoType)) {
            return PromoMutexConstants.GROUP_EXCLUSIVE_PRICE;
        }
        if (isShopPromo(promoType)) {
            return PromoMutexConstants.GROUP_SHOP_PROMO;
        }
        if (isCrossPromo(promoType)) {
            return PromoMutexConstants.GROUP_CROSS_PROMO;
        }
        return null;
    }

}
