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
 * 特价活动开停延迟消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpecialPriceDelayConsumer {

    private final PromoActivityMapper promoActivityMapper;
    private final IPromoActivityService promoActivityService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_SPECIAL_PRICE_START_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_SPECIAL_PRICE_START_DELAY
    ))
    public void handleSpecialPriceStart(Long activityId) {
        log.info("[特价延时] 活动 {} 到达开启时间", activityId);
        int updated = promoActivityMapper.updateStatusWithCas(activityId, 2L, 1L);
        if (updated > 0) {
            promoActivityService.cacheSpecialPricePromo(activityId);
            log.info("特价活动 {} 已变更为 [进行中] 并完成 Redis 预热", activityId);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_SPECIAL_PRICE_END_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_SPECIAL_PRICE_END_DELAY
    ))
    public void handleSpecialPriceEnd(Long activityId) {
        log.info("[特价延时] 活动 {} 到达结束时间", activityId);
        int updated = promoActivityMapper.updateStatusToEnded(activityId);
        if (updated > 0) {
            promoActivityService.clearSpecialPriceCache(activityId);
            log.info("特价活动 {} 已下架并清理 Redis 缓存", activityId);
        }
    }
}
