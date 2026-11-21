package org.dromara.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.order.api.mq.OrderAutoReceiveMessage;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderTimeConfig;
import org.dromara.order.domain.ShippingOrder;
import org.dromara.order.domain.ShippingOrderItem;
import org.dromara.order.domain.bo.MerchantShippingQueryBo;
import org.dromara.order.domain.bo.MerchantShippingShipBo;
import org.dromara.order.domain.vo.MerchantShippingDetailVo;
import org.dromara.order.domain.vo.ShippingOrderItemVo;
import org.dromara.order.domain.vo.ShippingOrderVo;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.mapper.OrderTimeConfigMapper;
import org.dromara.order.mapper.ShippingOrderItemMapper;
import org.dromara.order.mapper.ShippingOrderMapper;
import org.dromara.order.service.IMerchantShippingService;
import org.dromara.order.util.OrderPayStatusHelper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商家发货：发货单 10→20，订单 20→30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantShippingServiceImpl implements IMerchantShippingService {

    private static final Long SHIPPING_STATUS_WAIT = 10L;
    private static final Long SHIPPING_STATUS_SHIPPED = 20L;

    private static final Long ORDER_STATUS_PENDING_SHIP = 20L;
    private static final Long ORDER_STATUS_PENDING_RECEIVE = 30L;
    /** 在途售后：待审核 / 待退货 / 待验货 / 退款中 */
    private static final Long AS_PENDING_AUDIT = 10L;
    private static final Long AS_WAIT_RETURN = 20L;
    private static final Long AS_WAIT_INSPECT = 30L;
    private static final Long AS_REFUNDING = 40L;

    private static final long DEFAULT_AUTO_RECEIVE_DAYS = 7L;

    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 8L;

    private final RedissonClient redissonClient;
    private final PlatformTransactionManager transactionManager;
    private final RabbitTemplate rabbitTemplate;
    private final ShippingOrderMapper shippingOrderMapper;
    private final ShippingOrderItemMapper shippingOrderItemMapper;
    private final OrderMapper orderMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final OrderTimeConfigMapper orderTimeConfigMapper;

    @Override
    public TableDataInfo<ShippingOrderVo> queryPage(MerchantShippingQueryBo query, String tenantId, PageQuery pageQuery) {
        LambdaQueryWrapper<ShippingOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(ShippingOrder::getTenantId, tenantId);
        if (query != null) {
            lqw.eq(StringUtils.isNotBlank(query.getShippingNo()), ShippingOrder::getShippingNo, query.getShippingNo());
            lqw.eq(StringUtils.isNotBlank(query.getOrderNo()), ShippingOrder::getOrderNo, query.getOrderNo());
            lqw.eq(query.getShippingStatus() != null, ShippingOrder::getShippingStatus, query.getShippingStatus());
            lqw.eq(StringUtils.isNotBlank(query.getMemberCode()), ShippingOrder::getMemberCode, query.getMemberCode());
        }
        lqw.orderByDesc(ShippingOrder::getCreateTime);
        Page<ShippingOrderVo> page = shippingOrderMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public MerchantShippingDetailVo detail(String shippingNo, String tenantId, String memberCode) {
        if (StringUtils.isBlank(shippingNo)) {
            throw new ServiceException("发货单号不能为空");
        }
        ShippingOrder shipping = loadShipping(tenantId, shippingNo);
        if (shipping == null) {
            throw new ServiceException("发货单不存在");
        }
        if (StringUtils.isNotBlank(memberCode) && !memberCode.equals(shipping.getMemberCode())) {
            throw new ServiceException("无权查看该发货单");
        }
        MerchantShippingDetailVo detail = new MerchantShippingDetailVo();
        BeanUtils.copyProperties(shipping, detail);
        List<ShippingOrderItemVo> items = shippingOrderItemMapper.selectVoList(Wrappers.<ShippingOrderItem>lambdaQuery()
            .eq(ShippingOrderItem::getTenantId, tenantId)
            .eq(ShippingOrderItem::getShippingNo, shippingNo));
        detail.setItems(items);
        return detail;
    }

    @Override
    public void ship(MerchantShippingShipBo bo, String tenantId, String memberCode) {
        if (bo == null || StringUtils.isBlank(tenantId) || StringUtils.isBlank(memberCode)) {
            throw new ServiceException("参数不完整");
        }
        String company = StringUtils.trim(bo.getExpressCompany());
        String expressNo = StringUtils.trim(bo.getExpressNo());
        if (StringUtils.isBlank(company) || StringUtils.isBlank(expressNo)) {
            throw new ServiceException("快递公司与单号不能为空");
        }

        ShippingOrder shipping = loadShipping(tenantId, bo.getShippingNo());
        if (shipping == null) {
            throw new ServiceException("发货单不存在");
        }
        if (!memberCode.equals(shipping.getMemberCode())) {
            throw new ServiceException("无权操作该发货单");
        }
        String orderNo = shipping.getOrderNo();
        RLock lock = redissonClient.getLock(OrderCacheKeys.orderLifecycleLock(orderNo));
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new ServiceException("正在发货处理中，请勿重复操作");
            }
            try {
                new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    doShip(tenantId, memberCode, bo.getShippingNo(), company, expressNo));
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("发货处理中断，请稍后重试");
        }
    }

    private void doShip(String tenantId, String memberCode, String shippingNo,
                        String expressCompany, String expressNo) {
        ShippingOrder shipping = loadShipping(tenantId, shippingNo);
        if (shipping == null) {
            throw new ServiceException("发货单不存在");
        }
        if (!memberCode.equals(shipping.getMemberCode())) {
            throw new ServiceException("无权操作该发货单");
        }
        if (!SHIPPING_STATUS_WAIT.equals(shipping.getShippingStatus())) {
            throw new ServiceException("发货单状态不可发货");
        }

        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, shipping.getOrderNo())
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
        if (order == null) {
            throw new ServiceException("关联订单不存在");
        }
        OrderPayStatusHelper.requirePaidOrPartialRefund(order.getPayStatus());
        if (!ORDER_STATUS_PENDING_SHIP.equals(order.getOrderStatus())) {
            throw new ServiceException("订单非待发货状态，无法发货");
        }
        if (hasInTransitAfterSale(tenantId, shipping.getOrderNo())) {
            throw new ServiceException("订单售后处理中，暂不可发货");
        }

        Date now = new Date();
        int shipRows = shippingOrderMapper.shipByStatusCas(
            tenantId, shippingNo,
            SHIPPING_STATUS_WAIT, SHIPPING_STATUS_SHIPPED,
            expressCompany, expressNo, now);
        if (shipRows <= 0) {
            throw new ServiceException("发货失败，发货单可能已被处理");
        }

        int orderRows = orderMapper.markShippedByOrderNo(
            tenantId, shipping.getOrderNo(),
            ORDER_STATUS_PENDING_SHIP, ORDER_STATUS_PENDING_RECEIVE, now);
        if (orderRows <= 0) {
            throw new ServiceException("发货失败，订单状态已变更");
        }
        registerAutoReceiveAfterCommit(tenantId, order.getChannelCode(),
            shipping.getOrderNo(), shippingNo);
        log.info("商家发货成功 shippingNo={} orderNo={} expressNo={}",
            shippingNo, shipping.getOrderNo(), expressNo);
    }

    private void registerAutoReceiveAfterCommit(String tenantId, String channelCode,
                                                String orderNo, String shippingNo) {
        long days = resolveAutoReceiveDays(tenantId, channelCode);
        long ttlMillis = days * 24L * 60L * 60L * 1000L;
        OrderAutoReceiveMessage msg = OrderAutoReceiveMessage.builder()
            .tenantId(tenantId)
            .orderNo(orderNo)
            .shippingNo(shippingNo)
            .build();
        Runnable task = () -> {
            try {
                rabbitTemplate.convertAndSend(
                    OrderMqConstants.EXCHANGE_ORDER_DELAY,
                    OrderMqConstants.KEY_ORDER_AUTO_RECEIVE_DELAY,
                    msg,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlMillis);
                        return message;
                    });
                log.info("已投递自动确认收货延时消息 orderNo={} shippingNo={} days={}",
                    orderNo, shippingNo, days);
            } catch (Exception e) {
                log.error("自动确认收货延时消息投递失败 orderNo={} shippingNo={}",
                    orderNo, shippingNo, e);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private long resolveAutoReceiveDays(String tenantId, String channelCode) {
        if (StringUtils.isBlank(channelCode)) {
            return DEFAULT_AUTO_RECEIVE_DAYS;
        }
        OrderTimeConfig config = orderTimeConfigMapper.selectOne(Wrappers.<OrderTimeConfig>lambdaQuery()
            .eq(OrderTimeConfig::getTenantId, tenantId)
            .eq(OrderTimeConfig::getChannelCode, channelCode)
            .eq(OrderTimeConfig::getStatus, 1L)
            .last("LIMIT 1"));
        if (config != null && config.getAutoReceiveDays() != null && config.getAutoReceiveDays() > 0) {
            return config.getAutoReceiveDays();
        }
        return DEFAULT_AUTO_RECEIVE_DAYS;
    }

    private ShippingOrder loadShipping(String tenantId, String shippingNo) {
        return shippingOrderMapper.selectOne(Wrappers.<ShippingOrder>lambdaQuery()
            .eq(ShippingOrder::getTenantId, tenantId)
            .eq(ShippingOrder::getShippingNo, shippingNo)
            .last("LIMIT 1"));
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
