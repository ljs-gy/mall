package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.ShippingOrder;
import org.dromara.order.domain.ShippingOrderItem;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.mapper.ShippingOrderItemMapper;
import org.dromara.order.mapper.ShippingOrderMapper;
import org.dromara.order.service.IOrderPaidShippingCreateService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaidShippingCreateServiceImpl implements IOrderPaidShippingCreateService {

    private static final long PAY_STATUS_FULL_REFUND = 2L;
    private static final long SHIPPING_STATUS_WAIT = 10L;
    private static final long IS_GIFT = 1L;

    private final StringRedisTemplate stringRedisTemplate;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShippingOrderMapper shippingOrderMapper;
    private final ShippingOrderItemMapper shippingOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createShippingOnPaid(OrderPaidEvent event) {
        if (event == null || StringUtils.isBlank(event.getTenantId()) || StringUtils.isBlank(event.getOrderNo())) {
            log.warn("ORDER_PAID 建发货单参数不完整，跳过 event={}", event);
            return;
        }
        TenantHelper.dynamic(event.getTenantId(), () -> {
            String tenantId = event.getTenantId();
            String orderNo = event.getOrderNo();
            String claimKey = OrderCacheKeys.shippingCreateClaimed(orderNo);
            Boolean claimed = stringRedisTemplate.opsForValue().setIfAbsent(claimKey, "1", OrderCacheKeys.SHIPPING_CREATE_CLAIM_TTL_SECONDS, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(claimed)) {
                if (shippingExists(tenantId, orderNo)) {
                    log.info("ORDER_PAID 发货单已存在，幂等跳过 orderNo={}", orderNo);
                    return;
                }
                throw new ServiceException("创建发货单占坑未完成，触发重试 orderNo=" + orderNo);
            }
            try {
                createShipping(tenantId, orderNo);
            } catch (RuntimeException e) {
                stringRedisTemplate.delete(claimKey);
                throw e;
            }
        });
    }

    private void createShipping(String tenantId, String orderNo) {
        if (shippingExists(tenantId, orderNo)) {
            log.info("ORDER_PAID 发货单已存在，幂等跳过 orderNo={}", orderNo);
            return;
        }
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
        if (order == null) {
            log.warn("ORDER_PAID 建发货单子单不存在 orderNo={}", orderNo);
            return;
        }
        // 订单已退款，跳过，避免建发货单消息还在队列里堵着 / 失败重试，用户已经整单退款成功
        if (PAY_STATUS_FULL_REFUND == order.getPayStatus()) {
            log.info("ORDER_PAID 子单已全额退，跳过建发货单 orderNo={}", orderNo);
            return;
        }
        List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        List<ShippingOrderItem> shipItems = buildShipItems(tenantId, orderNo, orderItems);
        if (CollUtil.isEmpty(shipItems)) {
            log.info("ORDER_PAID 无可发数量，不建发货单 orderNo={}", orderNo);
            return;
        }
        String shippingNo = generateShippingNo(tenantId);
        ShippingOrder shipping = new ShippingOrder();
        shipping.setTenantId(tenantId);
        shipping.setShippingNo(shippingNo);
        shipping.setOrderNo(orderNo);
        shipping.setUserinfoId(order.getUserinfoId());
        shipping.setMemberBcode(order.getMemberBcode());
        shipping.setMemberCode(order.getMemberCode());
        shipping.setShippingStatus(SHIPPING_STATUS_WAIT);
        shipping.setConsigneeName(order.getConsigneeName());
        shipping.setConsigneeMobile(order.getConsigneeMobile());
        shipping.setReceiverAddressText(order.getReceiverAddressText());
        shippingOrderMapper.insert(shipping);
        for (ShippingOrderItem item : shipItems) {
            item.setShippingNo(shippingNo);
            shippingOrderItemMapper.insert(item);
        }
        log.info("ORDER_PAID 创建发货单成功 orderNo={} shippingNo={} itemCount={}", orderNo, shippingNo, shipItems.size());
    }

    private boolean shippingExists(String tenantId, String orderNo) {
        Long exists = shippingOrderMapper.selectCount(Wrappers.<ShippingOrder>lambdaQuery()
            .eq(ShippingOrder::getTenantId, tenantId)
            .eq(ShippingOrder::getOrderNo, orderNo));
        return exists != null && exists > 0;
    }

    private List<ShippingOrderItem> buildShipItems(String tenantId, String orderNo, List<OrderItem> orderItems) {
        List<ShippingOrderItem> result = new ArrayList<>();
        if (CollUtil.isEmpty(orderItems)) {
            return result;
        }
        for (OrderItem oi : orderItems) {
            if (oi.getIsGift() != null && oi.getIsGift() == IS_GIFT) {
                continue;
            }
            // 可发数量 = 购买数量 − 该行已退款成功的件数
            int refundedQty = oi.getRefundedQty() == null ? 0 : oi.getRefundedQty();
            int shipQty = oi.getQuantity().intValue() - refundedQty;
            if (shipQty <= 0) {
                continue;
            }
            ShippingOrderItem item = new ShippingOrderItem();
            item.setTenantId(tenantId);
            item.setOrderNo(orderNo);
            item.setOrderItemId(oi.getId());
            item.setSkuId(oi.getSkuId());
            item.setShippingQuantity((long) shipQty);
            result.add(item);
        }
        return result;
    }

    private String generateShippingNo(String tenantId) {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seqKey = "order:shipping:seq:" + timeStr;
        Long seq = stringRedisTemplate.opsForValue().increment(seqKey);
        if (seq != null && seq == 1L) {
            stringRedisTemplate.expire(seqKey, Duration.ofSeconds(5));
        }
        int tenantHash = Math.abs(tenantId.hashCode()) % 100;
        long finalSeq = (seq != null ? seq : ThreadLocalRandom.current().nextInt(1000, 9999)) % 1000000;
        return String.format("SH%s%02d%06d", timeStr, tenantHash, finalSeq);
    }
}
