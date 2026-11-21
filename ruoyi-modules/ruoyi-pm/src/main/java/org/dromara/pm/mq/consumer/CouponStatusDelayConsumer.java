package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.mapper.CouponTemplateMapper;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.ICouponTemplateService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 优惠券延迟消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponStatusDelayConsumer {

    private final CouponTemplateMapper couponTemplateMapper;
    private final ICouponTemplateService couponTemplateService;


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_COUPON_START_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_COUPON_START_DELAY
    ))
    public void handleCouponStartDelay(Long templateId) {
        log.info("收到优惠券 [自动开启] 延迟消息，模板ID: {}", templateId);
        int updated = couponTemplateMapper.updateStatusWithCas(templateId, 2L, 1L);
        if (updated > 0) {
            log.info("优惠券模板 {} 状态已自动切换为：2-进行中", templateId);
            couponTemplateService.cacheCouponTemplate(templateId);
            CouponTemplate template = couponTemplateMapper.selectById(templateId);
            if (template != null) {
                couponTemplateService.dispatchDirectSend(template);
            }
        } else {
            log.info("优惠券模板 {} 状态更新跳过（可能已被手动修改或已消费）", templateId);
        }
    }


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_COUPON_END_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_COUPON_END_DELAY
    ))
    public void handleCouponEndDelay(Long templateId) {
        log.info("收到优惠券 [自动结束] 延迟消息，模板ID: {}", templateId);
        int updated = couponTemplateMapper.updateStatusToEnded(templateId);
        if (updated > 0) {
            log.info("优惠券模板 {} 状态已自动切换为：3-已结束", templateId);
            couponTemplateService.clearCouponCache(templateId);
        }
    }


    /**
     * 定向券定时发放到点后的真正发券入口
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_COUPON_DIRECT_SEND_DELAY, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_COUPON_DIRECT_SEND_DELAY
    ))
    public void handleDirectSendDelay(Long templateId) {
        log.info("收到定向券 [定时发放] 延迟消息，模板ID: {}", templateId);
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            return;
        }
        if (!Long.valueOf(2L).equals(template.getStatus())) {
            log.warn("定向券模板 {} 当前状态 {} 非进行中，跳过发放", templateId, template.getStatus());
            return;
        }
        couponTemplateService.startDirectGrant(template);
    }
}
