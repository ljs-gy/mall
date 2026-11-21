package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.api.mq.OrderCancelledEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.pm.service.PromoAssetRollbackHelper;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledPromoConsumer {

    private final PromoAssetRollbackHelper promoAssetRollbackHelper;


    /**
     * 未支付取消消息，把下单时占住的营销资源还回去
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_ORDER_CANCELLED_PM, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_ORDER_CANCELLED, type = ExchangeTypes.FANOUT)
    ))
    public void onOrderCancelled(OrderCancelledEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId()) || StringUtils.isBlank(event.getOrderNo())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(), () -> {
            log.info("ORDER_CANCELLED 营销回滚开始 orderNo={} rollbackShared={}", event.getOrderNo(), event.getRollbackSharedAssets());
            promoAssetRollbackHelper.rollbackOnCancel(event);
            log.info("ORDER_CANCELLED 营销回滚完成 orderNo={}", event.getOrderNo());
        });
    }
}
