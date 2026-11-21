package org.dromara.order.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.order.service.IOrderPaidShippingCreateService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidShippingCreateConsumer {

    private final IOrderPaidShippingCreateService orderPaidShippingCreateService;

    /**
     * 支付成功消息到了，就给这个子单创建待发货的发货单。
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_PAID_SHIPPING_CREATE, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_PAID, type = ExchangeTypes.DIRECT),
        key = OrderMqConstants.KEY_ORDER_PAID
    ))
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId())) {
            return;
        }
        orderPaidShippingCreateService.createShippingOnPaid(event);
        log.info("ORDER_PAID 建发货单消费完成 orderNo={} parentOrderNo={}",
            event.getOrderNo(), event.getParentOrderNo());
    }
}
