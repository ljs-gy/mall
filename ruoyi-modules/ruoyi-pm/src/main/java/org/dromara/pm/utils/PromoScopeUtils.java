package org.dromara.pm.utils;

import org.dromara.pm.constant.PmCacheKeys;

/**
 * 营销范围工具
 */
public final class PromoScopeUtils {

    private PromoScopeUtils() {
    }


    public static Long resolveItemTargetCategory(Long scopeItemType) {
        if (scopeItemType == null) {
            return null;
        }
        if (Long.valueOf(1L).equals(scopeItemType)) {
            return PmCacheKeys.CAT_SKU;
        }
        if (Long.valueOf(2L).equals(scopeItemType)) {
            return PmCacheKeys.CAT_CATEGORY;
        }
        if (Long.valueOf(3L).equals(scopeItemType)) {
            return PmCacheKeys.CAT_BRAND;
        }
        return null;
    }

    public static boolean isSpecifiedItemScope(Long scopeItemType) {
        return Long.valueOf(1L).equals(scopeItemType)
            || Long.valueOf(2L).equals(scopeItemType)
            || Long.valueOf(3L).equals(scopeItemType);
    }

    public static boolean isSpecifiedUserScope(Long userScopeType) {
        return Long.valueOf(1L).equals(userScopeType)
            || Long.valueOf(2L).equals(userScopeType);
    }

}
