package org.dromara.pm.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.api.mq.AfterSaleRefundedEvent;
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
public class AfterSaleRefundedPromoConsumer {

    private final PromoAssetRollbackHelper promoAssetRollbackHelper;

    /**
     * 售后退款营销回滚
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_AFTER_SALE_REFUNDED_PM, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_AFTER_SALE_REFUNDED, type = ExchangeTypes.FANOUT)
    ))
    public void onAfterSaleRefunded(AfterSaleRefundedEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId()) || StringUtils.isBlank(event.getAfterSaleNo())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(), () -> {
            log.info("AFTER_SALE_REFUNDED 营销回滚开始 afterSaleNo={} rollbackShared={}", event.getAfterSaleNo(), event.getRollbackSharedAssets());
            promoAssetRollbackHelper.rollbackOnAfterSale(event);
            log.info("AFTER_SALE_REFUNDED 营销回滚完成 afterSaleNo={}", event.getAfterSaleNo());
        });
    }
}
