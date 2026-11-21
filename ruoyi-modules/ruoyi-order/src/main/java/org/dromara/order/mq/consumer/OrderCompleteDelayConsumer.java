package org.dromara.order.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.api.mq.OrderCompleteDelayMessage;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.service.IOrderCompleteService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 确认收货后过售后期延时消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompleteDelayConsumer {

    private final IOrderCompleteService orderCompleteService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_COMPLETE_DELAY, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_DELAY, delayed = "true"),
        key = OrderMqConstants.KEY_ORDER_COMPLETE_DELAY
    ))
    public void onCompleteDelay(OrderCompleteDelayMessage msg) {
        if (msg == null || StringUtils.isBlank(msg.getTenantId()) || StringUtils.isBlank(msg.getOrderNo())) {
            log.warn("交易完成延时消息不完整，跳过 msg={}", msg);
            return;
        }
        log.info("收到交易完成延时消息 orderNo={}", msg.getOrderNo());
        TenantHelper.dynamic(msg.getTenantId(),
            () -> orderCompleteService.onCompleteDelay(msg.getTenantId(), msg.getChannelCode(), msg.getOrderNo()));
    }
}
