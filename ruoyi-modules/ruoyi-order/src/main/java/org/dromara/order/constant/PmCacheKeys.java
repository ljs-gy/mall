package org.dromara.order.constant;

/**
 * 促销 / 优惠券 / 满赠发放规则 Redis Key。
 * <p>
 * {@code tenantCode}、{@code channelCode} 由调用方传入，不做缺省兜底。
 */
public final class PmCacheKeys {

    /** target_category: 供应商 */
    public static final long CAT_SUPPLIER = 1L;
    /** target_category: 分类 */
    public static final long CAT_CATEGORY = 2L;
    /** target_category: 品牌 */
    public static final long CAT_BRAND = 3L;
    /** target_category: SKU */
    public static final long CAT_SKU = 4L;

    private PmCacheKeys() {
    }

    private static String promoNs(String tenantCode, String channelCode) {
        return "promo:" + tenantCode + ":" + channelCode + ":";
    }

    private static String couponNs(String tenantCode, String channelCode) {
        return "coupon:" + tenantCode + ":" + channelCode + ":";
    }

    // -------------------- promo 共享集合 --------------------

    /** promo:{tenant}:{channel}:active_ids */
    public static String promoActiveIds(String tenantCode, String channelCode) {
        return promoNs(tenantCode, channelCode) + "active_ids";
    }

    /** promo:{tenant}:{channel}:scope:all */
    public static String promoScopeAll(String tenantCode, String channelCode) {
        return promoNs(tenantCode, channelCode) + "scope:all";
    }

    // -------------------- coupon 共享集合 --------------------

    /** coupon:{tenant}:{channel}:active_ids */
    public static String couponActiveIds(String tenantCode, String channelCode) {
        return couponNs(tenantCode, channelCode) + "active_ids";
    }

    /** coupon:{tenant}:{channel}:scope:all */
    public static String couponScopeAll(String tenantCode, String channelCode) {
        return couponNs(tenantCode, channelCode) + "scope:all";
    }

    /** coupon:{tenant}:{channel}:grant:active_ids */
    public static String couponGrantActiveIds(String tenantCode, String channelCode) {
        return couponNs(tenantCode, channelCode) + "grant:active_ids";
    }

    // -------------------- promo 独享 / 索引 --------------------

    /** promo:{tenant}:{channel}:detail:{activityId} */
    public static String promoDetail(String tenantCode, String channelCode, Long id) {
        return promoNs(tenantCode, channelCode) + "detail:" + id;
    }

    /** promo:{tenant}:{channel}:tiers:{activityId} */
    public static String promoTiers(String tenantCode, String channelCode, Long id) {
        return promoNs(tenantCode, channelCode) + "tiers:" + id;
    }

    /** promo:{tenant}:{channel}:budget:pool:{activityId} */
    public static String promoBudget(String tenantCode, String channelCode, Long id) {
        return promoNs(tenantCode, channelCode) + "budget:pool:" + id;
    }

    /** promo:{tenant}:{channel}:stock:{activityId} */
    public static String promoStock(String tenantCode, String channelCode, Long id) {
        return promoNs(tenantCode, channelCode) + "stock:" + id;
    }

    /** promo:{tenant}:{channel}:stock:{activityId}:{skuId} */
    public static String promoSkuStock(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "stock:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:sku:detail:{activityId}:{skuId} */
    public static String promoSkuDetail(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "sku:detail:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:seckill:sku:{activityId}:{skuId} */
    public static String seckillSku(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "seckill:sku:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:seckill:stock:{activityId}:{skuId} */
    public static String seckillStock(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "seckill:stock:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:seckill:user:limit:{activityId}:{skuId} */
    public static String seckillUserLimit(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "seckill:user:limit:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:special:sku:{activityId}:{skuId} */
    public static String specialSku(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "special:sku:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:special:stock:{activityId}:{skuId} */
    public static String specialStock(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "special:stock:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:special:user:limit:{activityId}:{skuId} */
    public static String specialUserLimit(String tenantCode, String channelCode, Long activityId, Long skuId) {
        return promoNs(tenantCode, channelCode) + "special:user:limit:" + activityId + ":" + skuId;
    }

    /** promo:{tenant}:{channel}:user:limit:{activityId} */
    public static String promoUserLimit(String tenantCode, String channelCode, Long id) {
        return promoNs(tenantCode, channelCode) + "user:limit:" + id;
    }

    /** promo:{tenant}:{channel}:user:{mode}:{activityId} */
    public static String promoUser(String tenantCode, String channelCode, String mode, Long id) {
        return promoNs(tenantCode, channelCode) + "user:" + mode + ":" + id;
    }

    /** promo:{tenant}:{channel}:level:{mode}:{activityId} */
    public static String promoLevel(String tenantCode, String channelCode, String mode, Long id) {
        return promoNs(tenantCode, channelCode) + "level:" + mode + ":" + id;
    }

    /** promo:{tenant}:{channel}:target:{dim}:{mode}:{activityId} （sku/brand/category 白黑名单） */
    public static String promoTarget(String tenantCode, String channelCode, String dim, String mode, Long id) {
        return promoNs(tenantCode, channelCode) + "target:" + dim + ":" + mode + ":" + id;
    }

    /** promo:{tenant}:{channel}:target:{dim}:{activityId} （supplier/site，无白黑） */
    public static String promoTarget(String tenantCode, String channelCode, String dim, Long id) {
        return promoNs(tenantCode, channelCode) + "target:" + dim + ":" + id;
    }

    /** promo:{tenant}:{channel}:scope:{category}:{targetId}（倒排仅白名单写入） */
    public static String promoScope(String tenantCode, String channelCode, Long category, Long targetId) {
        return promoNs(tenantCode, channelCode) + "scope:" + category + ":" + targetId;
    }

    // -------------------- coupon 独享 / 索引 --------------------

    /** coupon:{tenant}:{channel}:detail:{templateId} */
    public static String couponDetail(String tenantCode, String channelCode, Long id) {
        return couponNs(tenantCode, channelCode) + "detail:" + id;
    }

    /** coupon:{tenant}:{channel}:stock:{templateId} */
    public static String couponStock(String tenantCode, String channelCode, Long id) {
        return couponNs(tenantCode, channelCode) + "stock:" + id;
    }

    /** coupon:{tenant}:{channel}:user:limit:{templateId} */
    public static String couponUserLimit(String tenantCode, String channelCode, Long id) {
        return couponNs(tenantCode, channelCode) + "user:limit:" + id;
    }

    /** lock:coupon:claim:{tenant}:{channel}:{userId}:{templateId} */
    public static String couponClaimLock(String tenantCode, String channelCode, Long userId, Long templateId) {
        return "lock:coupon:claim:" + tenantCode + ":" + channelCode + ":" + userId + ":" + templateId;
    }

    /** coupon:{tenant}:{channel}:user:{mode}:{templateId} */
    public static String couponUser(String tenantCode, String channelCode, String mode, Long id) {
        return couponNs(tenantCode, channelCode) + "user:" + mode + ":" + id;
    }

    /** coupon:{tenant}:{channel}:level:{mode}:{templateId} */
    public static String couponLevel(String tenantCode, String channelCode, String mode, Long id) {
        return couponNs(tenantCode, channelCode) + "level:" + mode + ":" + id;
    }

    /** coupon:{tenant}:{channel}:target:{dim}:{mode}:{templateId} （sku/brand/category 白黑名单） */
    public static String couponTarget(String tenantCode, String channelCode, String dim, String mode, Long id) {
        return couponNs(tenantCode, channelCode) + "target:" + dim + ":" + mode + ":" + id;
    }

    /** coupon:{tenant}:{channel}:target:{dim}:{templateId} （supplier/site，无白黑） */
    public static String couponTarget(String tenantCode, String channelCode, String dim, Long id) {
        return couponNs(tenantCode, channelCode) + "target:" + dim + ":" + id;
    }

    /** coupon:{tenant}:{channel}:scope:{category}:{targetId}（倒排仅白名单写入） */
    public static String couponScope(String tenantCode, String channelCode, Long category, Long targetId) {
        return couponNs(tenantCode, channelCode) + "scope:" + category + ":" + targetId;
    }

    /** coupon:{tenant}:{channel}:grant:detail:{ruleId} */
    public static String couponGrantDetail(String tenantCode, String channelCode, Long ruleId) {
        return couponNs(tenantCode, channelCode) + "grant:detail:" + ruleId;
    }

    /** coupon:{tenant}:{channel}:grant:user:{mode}:{ruleId} */
    public static String couponGrantUser(String tenantCode, String channelCode, String mode, Long ruleId) {
        return couponNs(tenantCode, channelCode) + "grant:user:" + mode + ":" + ruleId;
    }

    /** coupon:{tenant}:{channel}:grant:level:{mode}:{ruleId} */
    public static String couponGrantLevel(String tenantCode, String channelCode, String mode, Long ruleId) {
        return couponNs(tenantCode, channelCode) + "grant:level:" + mode + ":" + ruleId;
    }

    /**
     * itemParticipateType / userParticipateType: 1白 2黑
     */
    public static String modeTag(Long participateType) {
        return Long.valueOf(2L).equals(participateType) ? "black" : "white";
    }

    /**
     * target_category -> 正向范围维度名
     */
    public static String targetDim(Long targetCategory) {
        if (targetCategory == null) {
            return null;
        }
        if (targetCategory == CAT_SUPPLIER) {
            return "supplier";
        }
        if (targetCategory == CAT_CATEGORY) {
            return "category";
        }
        if (targetCategory == CAT_BRAND) {
            return "brand";
        }
        if (targetCategory == CAT_SKU) {
            return "sku";
        }
        return null;
    }

    public static boolean isItemCategory(Long targetCategory) {
        return targetCategory != null
            && targetCategory >= CAT_CATEGORY
            && targetCategory <= CAT_SKU;
    }
}
