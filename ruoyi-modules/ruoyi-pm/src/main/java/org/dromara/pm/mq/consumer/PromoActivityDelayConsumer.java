package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.pm.mapper.PromoActivityMapper;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.IPromoActivityService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 满减/满折/满赠等促销活动开停延迟消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromoActivityDelayConsumer {

    private final PromoActivityMapper promoActivityMapper;
    private final IPromoActivityService promoActivityService;


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_PROMO_ACTIVITY_START_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_PROMO_ACTIVITY_START_DELAY
    ))
    public void handleActivityStartDelay(Long activityId) {
        log.info("[MQ] 满减活动 {} 到达开始时间，准备开启", activityId);
        int updated = promoActivityMapper.updateStatusWithCas(activityId, 2L, 1L);
        if (updated > 0) {
            promoActivityService.cachePromoActivity(activityId);
            log.info("满减活动 {} 已自动开启并完成 Redis 缓存预热", activityId);
        }
    }


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_PROMO_ACTIVITY_END_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_PROMO_ACTIVITY_END_DELAY
    ))
    public void handleActivityEndDelay(Long activityId) {
        log.info("[MQ] 满减活动 {} 到达结束时间，准备关停", activityId);
        int updated = promoActivityMapper.updateStatusToEnded(activityId);
        if (updated > 0) {
            promoActivityService.clearPromoActivityCache(activityId);
            log.info("满减活动 {} 已自动下架并清理缓存", activityId);
        }
    }
}
