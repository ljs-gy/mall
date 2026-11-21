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
 * 跨店满折开停延迟消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrossFullDiscountDelayConsumer {

    private final PromoActivityMapper promoActivityMapper;
    private final IPromoActivityService promoActivityService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_CROSS_DISCOUNT_START_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_CROSS_DISCOUNT_START_DELAY
    ))
    public void handleCrossDiscountStart(Long activityId) {
        log.info("[跨店满折延时] 活动 {} 到达开启时间", activityId);
        int updated = promoActivityMapper.updateStatusWithCas(activityId, 2L, 1L);
        if (updated > 0) {
            promoActivityService.cacheCrossFullDiscountPromo(activityId);
            log.info("跨店满折活动 {} 已变更为 [进行中] 并完成 Redis 预热", activityId);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_CROSS_DISCOUNT_END_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_CROSS_DISCOUNT_END_DELAY
    ))
    public void handleCrossDiscountEnd(Long activityId) {
        log.info("[跨店满折延时] 活动 {} 到达结束时间", activityId);
        int updated = promoActivityMapper.updateStatusToEnded(activityId);
        if (updated > 0) {
            promoActivityService.clearCrossFullDiscountCache(activityId);
            log.info("跨店满折活动 {} 已下架并清理 Redis 缓存", activityId);
        }
    }

}
