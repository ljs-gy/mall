package org.dromara.order.mq;

import org.dromara.order.api.mq.OrderMqConstants;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单取消广播交换机（由 order 声明，营销消费专享/共享回滚）
 */
@Configuration
public class OrderCancelledMqConfig {

    @Bean
    public FanoutExchange orderCancelledExchange() {
        return new FanoutExchange(OrderMqConstants.EXCHANGE_ORDER_CANCELLED, true, false);
    }
}
