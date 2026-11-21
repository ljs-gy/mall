package org.dromara.order.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderTimeoutCancelMessage;
import org.dromara.order.service.IOrderUnpaidCancelService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final IOrderUnpaidCancelService orderUnpaidCancelService;


    /**
     * 未支付超时关单
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_TIMEOUT_DELAY, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_DELAY, delayed = "true"),
        key = OrderMqConstants.KEY_ORDER_TIMEOUT_DELAY
    ))
    public void onOrderTimeoutMessage(OrderTimeoutCancelMessage msg) {
        if (msg == null || StringUtils.isBlank(msg.getOrderNo()) || StringUtils.isBlank(msg.getTenantId())) {
            log.warn("超时关单消息不完整，跳过 msg={}", msg);
            return;
        }
        log.info("收到子单超时延时关单消息 orderNo={} parentOrderNo={}", msg.getOrderNo(), msg.getParentOrderNo());
        orderUnpaidCancelService.cancelOnTimeout(msg.getTenantId(), msg.getOrderNo());
    }
}
