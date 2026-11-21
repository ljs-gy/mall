package org.dromara.order.mq;

import org.dromara.order.api.mq.OrderMqConstants;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 售后退款广播交换机（由 order 声明，保证发事件时 exchange 已存在）
 */
@Configuration
public class AfterSaleRefundedMqConfig {

    @Bean
    public FanoutExchange afterSaleRefundedExchange() {
        return new FanoutExchange(OrderMqConstants.EXCHANGE_AFTER_SALE_REFUNDED, true, false);
    }
}
