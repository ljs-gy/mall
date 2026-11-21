package org.dromara.order.constant;

/**
 * 订单域 Redis Key
 */
public final class OrderCacheKeys {

    private OrderCacheKeys() {
    }

    public static final long CANCEL_COMPENSATED_TTL_SECONDS = 7L * 24 * 3600;

    /**
     * order:cancel:compensated:{tenantId}:{orderNo}
     */
    public static String cancelCompensated(String tenantId, String orderNo) {
        return "order:cancel:compensated:" + tenantId + ":" + orderNo;
    }

    /**
     * 同父单支付通道互斥锁（预下单 / 主动取消 / 超时关单）
     * lock:pay:channel:{parentOrderNo}
     */
    public static String payChannelLock(String parentOrderNo) {
        return "lock:pay:channel:" + parentOrderNo;
    }

    /**
     * 子单生命周期主锁：售后申请 / 发货 / 待发货强制退款互斥
     * lock:order:lifecycle:{orderNo}
     */
    public static String orderLifecycleLock(String orderNo) {
        return "lock:order:lifecycle:" + orderNo;
    }

    /**
     * 支付成功创建发货单占坑（SETNX）
     * order:shipping:create:claimed:{orderNo}
     */
    public static final long SHIPPING_CREATE_CLAIM_TTL_SECONDS = 7L * 24 * 3600;

    public static String shippingCreateClaimed(String orderNo) {
        return "order:shipping:create:claimed:" + orderNo;
    }

    /**
     * 支付成功专享 sold 结转占坑（无 locked 后防 MQ 重投重复 +sold）
     * order:promo:sold:claimed:{tenantId}:{orderItemId}
     */
    public static final long PROMO_SOLD_CLAIM_TTL_SECONDS = 7L * 24 * 3600;

    public static String promoSoldClaimed(String tenantId, Long orderItemId) {
        return "order:promo:sold:claimed:" + tenantId + ":" + orderItemId;
    }
}
