package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.pm.mapper.CouponGiftGrantRuleMapper;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.ICouponGiftGrantRuleService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 满赠发放规则状态延迟变更消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GiftGrantRuleDelayConsumer {

    private final CouponGiftGrantRuleMapper giftGrantRuleMapper;
    private final ICouponGiftGrantRuleService giftGrantRuleService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_GIFT_RULE_START_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_GIFT_RULE_START_DELAY
    ))
    public void handleRuleStartDelay(Long ruleId) {
        log.info("收到满赠规则 [自动开启] 延迟消息，规则ID: {}", ruleId);
        int updated = giftGrantRuleMapper.updateStatusWithCas(ruleId, 2L, 1L);
        if (updated > 0) {
            giftGrantRuleService.cacheGiftGrantRule(ruleId);
            log.info("满赠规则 {} 状态已自动切换为：2-进行中，且已预热缓存", ruleId);
        } else {
            log.info("满赠规则 {} 状态跳过变更（当前状态可能已被手动调整）", ruleId);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_GIFT_RULE_END_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_GIFT_RULE_END_DELAY
    ))
    public void handleRuleEndDelay(Long ruleId) {
        log.info("收到满赠规则 [自动关停] 延迟消息，规则ID: {}", ruleId);
        int updated = giftGrantRuleMapper.updateStatusToEnded(ruleId);
        if (updated > 0) {
            giftGrantRuleService.clearGiftGrantRuleCache(ruleId);
            log.info("满赠规则 {} 状态已自动切换为：3-已结束，且已下架缓存", ruleId);
        }
    }
}
