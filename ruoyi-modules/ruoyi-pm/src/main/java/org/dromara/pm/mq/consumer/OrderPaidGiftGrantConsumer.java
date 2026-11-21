package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.pm.service.IOrderPaidGiftGrantService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidGiftGrantConsumer {

    private final IOrderPaidGiftGrantService orderPaidGiftGrantService;

    /**
     * 订单已支付，做满额送卷
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_PAID_GIFT_GRANT, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_PAID),
        key = OrderMqConstants.KEY_ORDER_PAID
    ))
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(),
            () -> orderPaidGiftGrantService.grantOnOrderPaid(event));
        log.info("ORDER_PAID 满额送券消费完成 parentOrderNo={}", event.getParentOrderNo());
    }
}
