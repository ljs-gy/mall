package org.dromara.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.mq.OrderCompleteDelayMessage;
import org.dromara.order.api.mq.OrderCompletedEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderTimeConfig;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.mapper.OrderTimeConfigMapper;
import org.dromara.order.service.IOrderCompleteService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

/**
 * 过售后期后发布交易完成事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCompleteServiceImpl implements IOrderCompleteService {

    private static final Long ORDER_STATUS_COMPLETED = 40L;
    private static final long DEFAULT_AUTO_COMPLETE_DAYS = 7L;
    private static final Long AS_PENDING_AUDIT = 10L;
    private static final Long AS_WAIT_RETURN = 20L;
    private static final Long AS_WAIT_INSPECT = 30L;
    private static final Long AS_REFUNDING = 40L;

    private final OrderMapper orderMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final OrderTimeConfigMapper orderTimeConfigMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void onCompleteDelay(String tenantId, String channelCode, String orderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo)) {
            return;
        }
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
        if (order == null) {
            log.warn("交易完成延时：订单不存在 orderNo={}", orderNo);
            return;
        }
        if (!ORDER_STATUS_COMPLETED.equals(order.getOrderStatus())) {
            log.info("交易完成延时：订单非已完成，跳过 orderNo={} status={}", orderNo, order.getOrderStatus());
            return;
        }
        String channel = StringUtils.blankToDefault(channelCode, order.getChannelCode());
        if (hasInTransitAfterSale(tenantId, orderNo)) {
            log.info("交易完成延时：售后处理中，再次延时 orderNo={}", orderNo);
            scheduleCompleteDelay(tenantId, channel, orderNo);
            return;
        }

        OrderCompletedEvent event = OrderCompletedEvent.builder()
            .tenantId(tenantId)
            .channelCode(channel)
            .orderNo(orderNo)
            .userId(order.getUserinfoId())
            .completeTime(new Date())
            .build();
        rabbitTemplate.convertAndSend(
            OrderMqConstants.EXCHANGE_ORDER_COMPLETED,
            OrderMqConstants.KEY_ORDER_COMPLETED,
            event);
        log.info("已发送 ORDER_COMPLETED orderNo={}", orderNo);
    }

    @Override
    public void scheduleCompleteDelay(String tenantId, String channelCode, String orderNo) {
        long days = resolveAutoCompleteDays(tenantId, channelCode);
        long ttlMillis = days * 24L * 60L * 60L * 1000L;
        OrderCompleteDelayMessage msg = OrderCompleteDelayMessage.builder()
            .tenantId(tenantId)
            .channelCode(channelCode)
            .orderNo(orderNo)
            .build();
        try {
            rabbitTemplate.convertAndSend(
                OrderMqConstants.EXCHANGE_ORDER_DELAY,
                OrderMqConstants.KEY_ORDER_COMPLETE_DELAY,
                msg,
                message -> {
                    message.getMessageProperties().setDelayLong(ttlMillis);
                    return message;
                });
            log.info("已投递交易完成延时消息 orderNo={} days={}", orderNo, days);
        } catch (Exception e) {
            log.error("交易完成延时消息投递失败 orderNo={}", orderNo, e);
        }
    }

    private long resolveAutoCompleteDays(String tenantId, String channelCode) {
        if (StringUtils.isBlank(channelCode)) {
            return DEFAULT_AUTO_COMPLETE_DAYS;
        }
        OrderTimeConfig config = orderTimeConfigMapper.selectOne(Wrappers.<OrderTimeConfig>lambdaQuery()
            .eq(OrderTimeConfig::getTenantId, tenantId)
            .eq(OrderTimeConfig::getChannelCode, channelCode)
            .eq(OrderTimeConfig::getStatus, 1L)
            .last("LIMIT 1"));
        if (config != null && config.getAutoCompleteDays() != null && config.getAutoCompleteDays() > 0) {
            return config.getAutoCompleteDays();
        }
        return DEFAULT_AUTO_COMPLETE_DAYS;
    }

    private boolean hasInTransitAfterSale(String tenantId, String orderNo) {
        Long cnt = afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getOrderNo, orderNo)
            .in(AfterSale::getStatus, Arrays.asList(
                AS_PENDING_AUDIT, AS_WAIT_RETURN, AS_WAIT_INSPECT, AS_REFUNDING)));
        return cnt != null && cnt > 0;
    }
}
