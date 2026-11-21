package org.dromara.dis.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.dis.service.IDistCommissionRefundService;
import org.dromara.order.api.mq.AfterSaleRefundedEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 售后退款成功 → 重算待结算佣金
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AfterSaleRefundedCommissionConsumer {

    private final IDistCommissionRefundService distCommissionRefundService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_AFTER_SALE_REFUNDED_DIS_COMMISSION, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_AFTER_SALE_REFUNDED, type = ExchangeTypes.FANOUT)
    ))
    public void onAfterSaleRefunded(AfterSaleRefundedEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId()) || StringUtils.isBlank(event.getOrderNo())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(),
            () -> distCommissionRefundService.recalcOnRefunded(event));
        log.info("AFTER_SALE_REFUNDED 分佣重算消费完成 orderNo={} afterSaleNo={}",
            event.getOrderNo(), event.getAfterSaleNo());
    }
}
