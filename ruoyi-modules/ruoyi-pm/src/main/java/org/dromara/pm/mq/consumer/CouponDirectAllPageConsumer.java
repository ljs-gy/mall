package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.pm.mq.CouponDirectAllPageMessage;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.ICouponTemplateService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 定向券全员直发分页接力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponDirectAllPageConsumer {

    private final ICouponTemplateService couponTemplateService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CouponMqConstants.QUEUE_COUPON_DIRECT_ALL_PAGE, durable = "true"),
        exchange = @Exchange(name = CouponMqConstants.EXCHANGE_PM_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = CouponMqConstants.KEY_COUPON_DIRECT_ALL_PAGE
    ))
    public void handleDirectAllPage(CouponDirectAllPageMessage message) {
        log.info("收到定向券全员直发分页消息 templateId={} lastUserId={}", message.getTemplateId(), message.getLastUserId());
        TenantHelper.dynamic(message.getTenantId(), () -> couponTemplateService.processDirectAllPage(message));
    }
}
