package org.dromara.dis.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.dis.service.IDistCommissionSettleService;
import org.dromara.order.api.mq.OrderCompletedEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 交易完成 → 分销佣金入账
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompletedCommissionSettleConsumer {

    private final IDistCommissionSettleService distCommissionSettleService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_COMPLETED_DIS_SETTLE, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_COMPLETED, type = ExchangeTypes.DIRECT),
        key = OrderMqConstants.KEY_ORDER_COMPLETED
    ))
    public void onOrderCompleted(OrderCompletedEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(),
            () -> distCommissionSettleService.settleOnCompleted(event));
        log.info("ORDER_COMPLETED 分佣结算消费完成 orderNo={}", event.getOrderNo());
    }
}
