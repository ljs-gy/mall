package org.dromara.order.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.OrderAutoReceiveMessage;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.service.IOrderReceiveService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 发货后延时自动确认收货
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoReceiveConsumer {

    private final IOrderReceiveService orderReceiveService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_AUTO_RECEIVE_DELAY, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_DELAY, delayed = "true"),
        key = OrderMqConstants.KEY_ORDER_AUTO_RECEIVE_DELAY
    ))
    public void onAutoReceive(OrderAutoReceiveMessage msg) {
        if (msg == null || StringUtils.isBlank(msg.getTenantId()) || StringUtils.isBlank(msg.getOrderNo())) {
            log.warn("自动确认收货消息不完整，跳过 msg={}", msg);
            return;
        }
        log.info("收到自动确认收货延时消息 orderNo={} shippingNo={}", msg.getOrderNo(), msg.getShippingNo());
        orderReceiveService.receiveBySystem(msg.getTenantId(), msg.getOrderNo());
    }
}
