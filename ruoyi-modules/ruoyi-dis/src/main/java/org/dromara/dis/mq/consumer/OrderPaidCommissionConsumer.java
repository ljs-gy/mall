package org.dromara.dis.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.dis.service.IDistCommissionCalcService;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付成功 → 异步生成分销待结算佣金
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidCommissionConsumer {

    private final IDistCommissionCalcService distCommissionCalcService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_PAID_DIS_COMMISSION, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_PAID, type = ExchangeTypes.DIRECT),
        key = OrderMqConstants.KEY_ORDER_PAID
    ))
    public void onOrderPaid(OrderPaidEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(),
            () -> distCommissionCalcService.createPendingOnPaid(event));
        log.info("ORDER_PAID 分佣消费完成 orderNo={}", event.getOrderNo());
    }
}
