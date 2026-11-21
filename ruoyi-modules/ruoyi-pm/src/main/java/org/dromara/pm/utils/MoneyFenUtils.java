package org.dromara.pm.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额元 ↔ 分转换（跨店预算池等 Redis 整数口径）。
 * <p>
 * 扣减与回补必须使用同一规则，避免 HALF_UP 与截断不一致导致 1 分偏差。
 * </p>
 */
public final class MoneyFenUtils {

    private MoneyFenUtils() {
    }

    /**
     * 元转分：先规范到分位（2 位小数 HALF_UP），再移两位得到整数分。
     */
    public static long toFen(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact();
    }
}
