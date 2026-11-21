package org.dromara.order.util;

import org.dromara.common.core.exception.ServiceException;

/**
 * 订单支付状态门禁：0未支付 1已支付 2全额退 3部分退
 */
public final class OrderPayStatusHelper {

    public static final Long UNPAID = 0L;
    public static final Long PAID = 1L;
    public static final Long FULL_REFUND = 2L;
    public static final Long PARTIAL_REFUND = 3L;

    private OrderPayStatusHelper() {
    }

    /** 已支付或部分退：可继续申请售后 / 发货 / 强制退剩余 */
    public static boolean allowAfterSaleOrFulfillment(Long payStatus) {
        return PAID.equals(payStatus) || PARTIAL_REFUND.equals(payStatus);
    }

    public static void requirePaidOrPartialRefund(Long payStatus) {
        if (allowAfterSaleOrFulfillment(payStatus)) {
            return;
        }
        if (FULL_REFUND.equals(payStatus)) {
            throw new ServiceException("订单已全额退款");
        }
        throw new ServiceException("订单未支付");
    }
}
