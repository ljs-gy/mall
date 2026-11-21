package org.dromara.order.mq;

import org.dromara.order.api.mq.OrderMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单支付成功 Direct 交换机。
 * 同一 routing key 绑定多个队列，实现并行订阅（资产结转 / 满额送券）。
 */
@Configuration
public class OrderPaidMqConfig {

    @Bean
    public DirectExchange orderPaidExchange() {
        return new DirectExchange(OrderMqConstants.EXCHANGE_ORDER_PAID, true, false);
    }

    @Bean
    public Queue orderPaidAssetConfirmQueue() {
        return new Queue(OrderMqConstants.QUEUE_ORDER_PAID_ASSET_CONFIRM, true);
    }

    @Bean
    public Binding orderPaidAssetConfirmBinding(Queue orderPaidAssetConfirmQueue, DirectExchange orderPaidExchange) {
        return BindingBuilder.bind(orderPaidAssetConfirmQueue)
            .to(orderPaidExchange)
            .with(OrderMqConstants.KEY_ORDER_PAID);
    }

    @Bean
    public Queue orderPaidGiftGrantQueue() {
        return new Queue(OrderMqConstants.QUEUE_ORDER_PAID_GIFT_GRANT, true);
    }

    @Bean
    public Binding orderPaidGiftGrantBinding(Queue orderPaidGiftGrantQueue, DirectExchange orderPaidExchange) {
        return BindingBuilder.bind(orderPaidGiftGrantQueue)
            .to(orderPaidExchange)
            .with(OrderMqConstants.KEY_ORDER_PAID);
    }

    @Bean
    public Queue orderPaidShippingCreateQueue() {
        return new Queue(OrderMqConstants.QUEUE_ORDER_PAID_SHIPPING_CREATE, true);
    }

    @Bean
    public Binding orderPaidShippingCreateBinding(Queue orderPaidShippingCreateQueue, DirectExchange orderPaidExchange) {
        return BindingBuilder.bind(orderPaidShippingCreateQueue)
            .to(orderPaidExchange)
            .with(OrderMqConstants.KEY_ORDER_PAID);
    }
}
