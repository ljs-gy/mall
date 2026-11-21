package org.dromara.order.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.AfterSaleRefundTriggerMessage;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.service.IAfterSaleRefundService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AfterSaleRefundTriggerConsumer {

    private final IAfterSaleRefundService afterSaleRefundService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_AFTER_SALE_REFUND_TRIGGER, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_AFTER_SALE_REFUND_TRIGGER, type = ExchangeTypes.DIRECT),
        key = OrderMqConstants.KEY_AFTER_SALE_REFUND_TRIGGER
    ))
    public void onRefundTrigger(AfterSaleRefundTriggerMessage message) {
        if (message == null
            || StringUtils.isBlank(message.getTenantId())
            || StringUtils.isBlank(message.getAfterSaleNo())) {
            log.warn("售后退款触发消息非法，丢弃 message={}", message);
            return;
        }
        afterSaleRefundService.executeRefundTrigger(message.getTenantId(), message.getAfterSaleNo());
        log.info("售后退款触发消费完成 afterSaleNo={}", message.getAfterSaleNo());
    }
}
