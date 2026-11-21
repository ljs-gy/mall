package org.dromara.pm.mq;

/**
 * PM 延时开停 / 定向发放等 RabbitMQ 常量
 */
public interface CouponMqConstants {

    /** PM 统一延时交换机（券 / 活动 / 满赠规则开停） */
    String EXCHANGE_PM_DELAY = "exchange.pm.delay";

    String KEY_COUPON_START_DELAY = "key.coupon.start.delay";
    String KEY_COUPON_END_DELAY = "key.coupon.end.delay";
    String KEY_COUPON_DIRECT_SEND_DELAY = "key.coupon.direct.send.delay";
    /** 定向券全员直发：按页接力（消息内带 lastUserId，无任务表） */
    String KEY_COUPON_DIRECT_ALL_PAGE = "key.coupon.direct.all.page";

    String QUEUE_COUPON_START_DELAY = "queue.pm.coupon.start.delay";
    String QUEUE_COUPON_END_DELAY = "queue.pm.coupon.end.delay";
    String QUEUE_COUPON_DIRECT_SEND_DELAY = "queue.pm.coupon.direct.send.delay";
    String QUEUE_COUPON_DIRECT_ALL_PAGE = "queue.pm.coupon.direct.all.page";

    String KEY_GIFT_RULE_START_DELAY = "key.gift.rule.start.delay";
    String KEY_GIFT_RULE_END_DELAY = "key.gift.rule.end.delay";

    String QUEUE_GIFT_RULE_START_DELAY = "queue.pm.gift.rule.start.delay";
    String QUEUE_GIFT_RULE_END_DELAY = "queue.pm.gift.rule.end.delay";

    String KEY_PROMO_ACTIVITY_START_DELAY = "key.promo.activity.start.delay";
    String KEY_PROMO_ACTIVITY_END_DELAY = "key.promo.activity.end.delay";

    String QUEUE_PROMO_ACTIVITY_START_DELAY = "queue.pm.promo.activity.start.delay";
    String QUEUE_PROMO_ACTIVITY_END_DELAY = "queue.pm.promo.activity.end.delay";

    String KEY_SECKILL_START_DELAY = "key.seckill.start.delay";
    String KEY_SECKILL_END_DELAY = "key.seckill.end.delay";

    String QUEUE_SECKILL_START_DELAY = "queue.pm.seckill.start.delay";
    String QUEUE_SECKILL_END_DELAY = "queue.pm.seckill.end.delay";

    String KEY_SPECIAL_PRICE_START_DELAY = "key.special.price.start.delay";
    String KEY_SPECIAL_PRICE_END_DELAY = "key.special.price.end.delay";

    String QUEUE_SPECIAL_PRICE_START_DELAY = "queue.pm.special.price.start.delay";
    String QUEUE_SPECIAL_PRICE_END_DELAY = "queue.pm.special.price.end.delay";

    String KEY_CROSS_REDUCE_START_DELAY = "key.cross.reduce.start.delay";
    String KEY_CROSS_REDUCE_END_DELAY = "key.cross.reduce.end.delay";

    String QUEUE_CROSS_REDUCE_START_DELAY = "queue.pm.cross.reduce.start.delay";
    String QUEUE_CROSS_REDUCE_END_DELAY = "queue.pm.cross.reduce.end.delay";

    String KEY_CROSS_DISCOUNT_START_DELAY = "key.cross.discount.start.delay";
    String KEY_CROSS_DISCOUNT_END_DELAY = "key.cross.discount.end.delay";

    String QUEUE_CROSS_DISCOUNT_START_DELAY = "queue.pm.cross.discount.start.delay";
    String QUEUE_CROSS_DISCOUNT_END_DELAY = "queue.pm.cross.discount.end.delay";

}
