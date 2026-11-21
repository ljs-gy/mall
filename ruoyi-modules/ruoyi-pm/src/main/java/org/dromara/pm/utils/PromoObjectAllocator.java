package org.dromara.pm.utils;

import cn.hutool.core.collection.CollUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * PromoObjectAllocator 是通用金额分摊工具：把一笔总优惠，按各对象自己的「基数」比例拆开，并直接写回对象
 */
public final class PromoObjectAllocator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private PromoObjectAllocator() {
    }

    /**
     * 把一笔钱按比例拆到对象上并写回去。
     * @param items           已筛好的合资格对象列表
     * @param baseGetter      从对象取分摊基数
     * @param shareSetter     把分到的金额写回对象
     * @param totalToAllocate 要摊的总额
     */
    public static <T> void allocate(List<T> items, Function<T, BigDecimal> baseGetter, BiConsumer<T, BigDecimal> shareSetter, BigDecimal totalToAllocate) {
        if (CollUtil.isEmpty(items) || totalToAllocate == null || totalToAllocate.compareTo(ZERO) <= 0 || baseGetter == null || shareSetter == null) {
            return;
        }
        // 有效项的基数合计
        BigDecimal sumBase = ZERO;
        // 真正能参与分摊的对象列表
        List<T> activeItems = new ArrayList<>();
        for (T item : items) {
            if (item == null) {
                continue;
            }
            BigDecimal base = baseGetter.apply(item);
            if (base != null && base.compareTo(ZERO) > 0) {
                activeItems.add(item);
                sumBase = sumBase.add(base);
            }
        }
        if (activeItems.isEmpty() || sumBase.compareTo(ZERO) <= 0) {
            return;
        }
        // 真正能分摊的钱，不能超过该行基数合计
        BigDecimal actualTotal = totalToAllocate.min(sumBase);
        // 把参与分摊的对象按基数从小到大排：小的在前
        activeItems.sort(Comparator.comparing(baseGetter));
        // 前面已经分摊的金额
        BigDecimal allocated = ZERO;
        int size = activeItems.size();
        for (int i = 0; i < size; i++) {
            T item = activeItems.get(i);
            // 当前行的分摊基数
            BigDecimal base = baseGetter.apply(item);
            // 当前项分到的优惠
            BigDecimal share;
            if (i == size - 1) {
                share = actualTotal.subtract(allocated).max(ZERO).min(base);
            } else {
                // 这一项分到的优惠 = 总优惠 *   （当前这一项的分摊基数 / 所有参与项的基数总和 ）
                share = actualTotal.multiply(base)
                    .divide(sumBase, 2, RoundingMode.DOWN)
                    .min(base)
                    .max(ZERO);
            }
            shareSetter.accept(item, share);
            allocated = allocated.add(share);
        }
    }
}
