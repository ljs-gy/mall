package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.ShippingOrder;
import org.dromara.order.domain.bo.AfterSaleAuditCommand;
import org.dromara.order.domain.bo.AfterSaleAuditCommand.AuditorType;
import org.dromara.order.domain.bo.PendingShipForceRefundCommand;
import org.dromara.order.domain.bo.PendingShipForceRefundCommand.OperatorType;
import org.dromara.order.domain.bo.PcAfterSaleApplyItemBo;
import org.dromara.order.domain.vo.PcAfterSaleApplyResultVo;
import org.dromara.order.mapper.AfterSaleItemMapper;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.mapper.ShippingOrderMapper;
import org.dromara.order.service.IAfterSaleAuditService;
import org.dromara.order.service.IPendingShipForceRefundService;
import org.dromara.order.util.OrderItemRefundOccupyWriter;
import org.dromara.order.util.OrderPayStatusHelper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 待发货强制仅退款：按 items 新建仅退款并自动审核通过。
 * 有进行中售后时拒绝；支持部分退，整单退由前端勾选全部可退行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingShipForceRefundServiceImpl implements IPendingShipForceRefundService {

    private static final long SERVICE_REFUND_ONLY = 1L;
    private static final long APPLY_SOURCE_MERCHANT = 2L;
    private static final long APPLY_SOURCE_OPS = 3L;

    private static final long STATUS_PENDING_AUDIT = 10L;
    private static final long STATUS_WAIT_RETURN = 20L;
    private static final long STATUS_WAIT_INSPECT = 30L;
    private static final long STATUS_REFUNDING = 40L;

    private static final long ORDER_STATUS_PENDING_SHIP = 20L;
    private static final long SHIPPING_STATUS_SHIPPED = 20L;
    private static final long SHIPPING_STATUS_RECEIVED = 30L;

    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 15L;

    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleItemMapper afterSaleItemMapper;
    private final ShippingOrderMapper shippingOrderMapper;
    private final IAfterSaleAuditService afterSaleAuditService;
    private final OrderItemRefundOccupyWriter orderItemRefundOccupyWriter;

    @Lazy
    @Autowired
    private PendingShipForceRefundServiceImpl self;

    @Override
    public PcAfterSaleApplyResultVo forceRefund(PendingShipForceRefundCommand command) {
        if (command == null
            || StringUtils.isBlank(command.getTenantId())
            || StringUtils.isBlank(command.getOrderNo())
            || command.getOperatorType() == null
            || CollUtil.isEmpty(command.getItems())) {
            throw new ServiceException("退款参数不完整");
        }
        if (command.getOperatorType() == OperatorType.MERCHANT
            && StringUtils.isBlank(command.getMemberCode())) {
            throw new ServiceException("商家编码不能为空");
        }
        String orderNo = command.getOrderNo();
        RLock lock = redissonClient.getLock(OrderCacheKeys.orderLifecycleLock(orderNo));
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new ServiceException("订单正在处理售后或发货，请稍后重试");
            }
            try {
                return self.doForceRefund(command);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("退款处理中断，请稍后重试");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PcAfterSaleApplyResultVo doForceRefund(PendingShipForceRefundCommand command) {
        String tenantId = command.getTenantId();
        String orderNo = command.getOrderNo();
        String applyReason = command.getApplyReason();
        AuditorType auditorType = command.getOperatorType() == OperatorType.OPS ? AuditorType.OPS : AuditorType.MERCHANT;
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1"));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (command.getOperatorType() == OperatorType.MERCHANT) {
            if (StringUtils.isBlank(command.getMemberCode())
                || !command.getMemberCode().equals(order.getMemberCode())) {
                throw new ServiceException("无权操作该店铺订单");
            }
        } else if (command.getOperatorType() != OperatorType.OPS) {
            throw new ServiceException("不支持的操作角色");
        }
        // 只允许已支付、部分退
        OrderPayStatusHelper.requirePaidOrPartialRefund(order.getPayStatus());
        if (ORDER_STATUS_PENDING_SHIP != order.getOrderStatus()) {
            throw new ServiceException("仅待发货订单可强制退款");
        }
        Long shipped = shippingOrderMapper.selectCount(Wrappers.<ShippingOrder>lambdaQuery()
            .eq(ShippingOrder::getTenantId, tenantId)
            .eq(ShippingOrder::getOrderNo, orderNo)
            .in(ShippingOrder::getShippingStatus, Arrays.asList(SHIPPING_STATUS_SHIPPED, SHIPPING_STATUS_RECEIVED)));
        if (shipped != null && shipped > 0) {
            throw new ServiceException("订单已发货，无法待发货强制退款");
        }
        // 存在待审 / 待退货 / 待验货 / 退款中任一状态就报错，需先处理完再强制退
        assertNoBlockingAfterSale(tenantId, orderNo);
        PcAfterSaleApplyResultVo result = createRefundOnlyAfterSale(command, order, applyReason);
        afterSaleAuditService.audit(AfterSaleAuditCommand.builder()
            .tenantId(tenantId)
            .afterSaleNo(result.getAfterSaleNo())
            .pass(true)
            .auditRemark(applyReason)
            .auditorType(auditorType)
            .build());
        return result;
    }

    private void assertNoBlockingAfterSale(String tenantId, String orderNo) {
        Long blocking = afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getOrderNo, orderNo)
            .in(AfterSale::getStatus, Arrays.asList(
                STATUS_PENDING_AUDIT, STATUS_WAIT_RETURN, STATUS_WAIT_INSPECT, STATUS_REFUNDING)));
        if (blocking != null && blocking > 0) {
            throw new ServiceException("订单存在进行中的售后，请先处理后再强制退款");
        }
    }

    private PcAfterSaleApplyResultVo createRefundOnlyAfterSale(PendingShipForceRefundCommand command, Order order, String applyReason) {
        String tenantId = command.getTenantId();
        String orderNo = order.getOrderNo();
        List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, orderNo));
        if (CollUtil.isEmpty(orderItems)) {
            throw new ServiceException("订单明细不存在");
        }
        Map<Long, OrderItem> itemMap = orderItems.stream()
            .collect(Collectors.toMap(OrderItem::getId, i -> i, (a, b) -> a));
        List<AfterSaleItem> afterItems = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (PcAfterSaleApplyItemBo applyItem : command.getItems()) {
            Long orderItemId = applyItem.getOrderItemId();
            OrderItem oi = itemMap.get(orderItemId);
            if (oi.getIsGift() != null && oi.getIsGift() == 1L) {
                throw new ServiceException("赠品不能单独强制退款，主品退完后系统自动收回");
            }
            int askQty = applyItem.getReturnQuantity();
            int buyQty = oi.getQuantity().intValue();
            int refundedQty = nullToZero(oi.getRefundedQty());
            int refundingQty = nullToZero(oi.getRefundingQty());
            int available = buyQty - refundedQty - refundingQty;
            if (askQty > available) {
                throw new ServiceException("商品【"
                    + StringUtils.blankToDefault(oi.getGoodsTitle(), String.valueOf(oi.getSkuId()))
                    + "】可退数量不足，剩余可退 " + Math.max(available, 0));
            }
            BigDecimal lineRefund = calcLineRefund(oi, askQty, available,
                nullToZero(oi.getRefundedAmount()), nullToZero(oi.getRefundingAmount()));
            afterItems.add(buildAfterSaleItem(tenantId, orderNo, oi, askQty, lineRefund));
            totalRefund = totalRefund.add(lineRefund);
        }
        String newAfterSaleNo = generateAfterSaleNo(tenantId);
        AfterSale afterSale = new AfterSale();
        afterSale.setTenantId(tenantId);
        afterSale.setAfterSaleNo(newAfterSaleNo);
        afterSale.setOrderNo(orderNo);
        afterSale.setUserinfoId(order.getUserinfoId());
        afterSale.setMemberBcode(order.getMemberBcode());
        afterSale.setMemberCode(order.getMemberCode());
        afterSale.setServiceType(SERVICE_REFUND_ONLY);
        afterSale.setApplySource(command.getOperatorType() == OperatorType.OPS ? APPLY_SOURCE_OPS : APPLY_SOURCE_MERCHANT);
        afterSale.setStatus(STATUS_PENDING_AUDIT);
        afterSale.setRefundAmount(totalRefund);
        afterSale.setApplyReason(applyReason);
        afterSale.setApplyRemark(applyReason);
        afterSaleMapper.insert(afterSale);
        for (AfterSaleItem asi : afterItems) {
            asi.setAfterSaleNo(newAfterSaleNo);
            afterSaleItemMapper.insert(asi);
        }
        orderItemRefundOccupyWriter.addRefunding(afterItems);
        log.info("待发货强制退款：已创建仅退款售后 afterSaleNo={} orderNo={} source={} refundAmount={}", newAfterSaleNo, orderNo, afterSale.getApplySource(), totalRefund);
        PcAfterSaleApplyResultVo result = new PcAfterSaleApplyResultVo();
        result.setAfterSaleNo(newAfterSaleNo);
        result.setRefundAmount(totalRefund);
        return result;
    }

    private AfterSaleItem buildAfterSaleItem(String tenantId, String orderNo, OrderItem oi, int qty, BigDecimal lineRefund) {
        AfterSaleItem asi = new AfterSaleItem();
        asi.setTenantId(tenantId);
        asi.setOrderNo(orderNo);
        asi.setOrderItemId(oi.getId());
        asi.setSkuId(oi.getSkuId());
        asi.setSkuCode(oi.getSkuCode());
        asi.setGoodsTitle(oi.getGoodsTitle());
        asi.setSpecText(oi.getSpecText());
        asi.setCoverImage(oi.getCoverImage());
        asi.setReturnQuantity(qty);
        asi.setRefundPrice(oi.getSharePrice());
        asi.setRefundAmount(lineRefund);
        return asi;
    }

    /**
     * 计算本行本次退款金额。
     *
     * @param oi           订单明细
     * @param askQty       本次退货件数
     * @param available    本行当前可退件数（购买 − 已退成功 − 在途）
     * @param successAmt   本行已退款成功金额
     * @param inTransitAmt 本行在途售后占用金额
     */
    private BigDecimal calcLineRefund(OrderItem oi, int askQty, int available, BigDecimal successAmt, BigDecimal inTransitAmt) {
        // 退光剩余：用行实付减去已退成功与在途，避免单价乘除产生尾差
        if (askQty == available) {
            return oi.getRealPayAmount().subtract(successAmt).subtract(inTransitAmt);
        }
        // 部分退：分摊单价 × 件数
        return oi.getSharePrice().multiply(BigDecimal.valueOf(askQty));
    }

    private String generateAfterSaleNo(String tenantId) {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seqKey = "order:afterSale:seq:" + timeStr;
        Long seq = stringRedisTemplate.opsForValue().increment(seqKey);
        if (seq != null && seq == 1L) {
            stringRedisTemplate.expire(seqKey, Duration.ofSeconds(5));
        }
        int tenantHash = Math.abs(tenantId.hashCode()) % 100;
        long finalSeq = (seq != null ? seq : ThreadLocalRandom.current().nextInt(1000, 9999)) % 1000000;
        return String.format("AS%s%02d%06d", timeStr, tenantHash, finalSeq);
    }

    private static int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
