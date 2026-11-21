package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.Order;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderCancelCompensateService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelCompensateServiceImpl implements IOrderCancelCompensateService {

    private static final Long ORDER_STATUS_CANCELLED = 50L;

    private final StringRedisTemplate stringRedisTemplate;
    private final OrderMapper orderMapper;

    @Override
    public boolean isCancelCompensated(String tenantId, String orderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo)) {
            return false;
        }
        return stringRedisTemplate.hasKey(OrderCacheKeys.cancelCompensated(tenantId, orderNo));
    }

    @Override
    public void markCancelCompensated(String tenantId, String orderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(orderNo)) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
            OrderCacheKeys.cancelCompensated(tenantId, orderNo),
            "1",
            OrderCacheKeys.CANCEL_COMPENSATED_TTL_SECONDS,
            TimeUnit.SECONDS);
        log.info("已标记取消补偿完成 orderNo={}", orderNo);
    }

    @Override
    public boolean allSiblingsCancelled(String tenantId, String parentOrderNo) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(parentOrderNo)) {
            return false;
        }
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getParentOrderNo, parentOrderNo)
            .eq(Order::getDelFlag, "0"));
        if (CollUtil.isEmpty(orders)) {
            return false;
        }
        return orders.stream().allMatch(o -> ORDER_STATUS_CANCELLED.equals(o.getOrderStatus()));
    }
}
