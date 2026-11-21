package org.dromara.pm.enums;

/**
 * 营销互斥组常量
 */
public final class PromoMutexConstants {

    private PromoMutexConstants() {
    }

    /** 单品超级排他组：秒杀/特价 */
    public static final String GROUP_EXCLUSIVE_PRICE = "EXCLUSIVE_PRICE_GROUP";

    /** 店铺营销活动组：满减/满折/满赠 */
    public static final String GROUP_SHOP_PROMO = "SHOP_PROMO_GROUP";

    /** 平台跨店活动组：跨店满减/跨店满折 */
    public static final String GROUP_CROSS_PROMO = "CROSS_PROMO_GROUP";

    /** 同组互斥择优（三选一/多选一，取优惠最大） */
    public static final long MUTEX_RULE_PICK_BEST = 1L;

    /** 绝对超级排他（阻断后续活动与优惠券） */
    public static final long MUTEX_RULE_ABSOLUTE = 2L;

}
