package org.dromara.order.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.order.service.IOrderPaidAssetConfirmService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidAssetConfirmConsumer {

    private final IOrderPaidAssetConfirmService orderPaidAssetConfirmService;

    /**
     * 支付成功后，把下单时占用的库存/专享等从「预占」结转到「已售出确认」。
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_PAID_ASSET_CONFIRM, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_PAID, type = ExchangeTypes.DIRECT),
        key = OrderMqConstants.KEY_ORDER_PAID
    ))
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId())) {
            return;
        }
        orderPaidAssetConfirmService.confirmAssets(event);
        log.info("ORDER_PAID 资产结转消费完成 orderNo={} parentOrderNo={}",
            event.getOrderNo(), event.getParentOrderNo());
    }
}
