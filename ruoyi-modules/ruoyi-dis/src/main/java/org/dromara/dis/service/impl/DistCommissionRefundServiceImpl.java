package org.dromara.dis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.dis.domain.DistCommissionItem;
import org.dromara.dis.domain.DistCommissionOrder;
import org.dromara.dis.enums.DistCommissionStatus;
import org.dromara.dis.mapper.DistCommissionItemMapper;
import org.dromara.dis.mapper.DistCommissionOrderMapper;
import org.dromara.dis.service.IDistCommissionRefundService;
import org.dromara.order.api.mq.AfterSaleRefundedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * 退款成功 → 按剩余金额重算待结算佣金（不碰已结算）
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistCommissionRefundServiceImpl implements IDistCommissionRefundService {

    private static final String CANCEL_REASON_REFUND = "REFUND";

    private final DistCommissionOrderMapper commissionOrderMapper;
    private final DistCommissionItemMapper commissionItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalcOnRefunded(AfterSaleRefundedEvent event) {
        if (event == null || StringUtils.isBlank(event.getOrderNo())) {
            log.warn("AFTER_SALE_REFUNDED 分佣重算参数不完整，跳过 event={}", event);
            return;
        }

        DistCommissionOrder order = commissionOrderMapper.selectOne(
            Wrappers.<DistCommissionOrder>lambdaQuery()
                .eq(DistCommissionOrder::getOrderNo, event.getOrderNo())
                .last("LIMIT 1")
        );
        if (order == null) {
            log.info("AFTER_SALE_REFUNDED 无分佣单，跳过 orderNo={}", event.getOrderNo());
            return;
        }

        BigDecimal payAmount = nullToZero(event.getPayAmount());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            // 事件缺原实付时回落分佣头表首次记的基数（支付成功时写入）
            payAmount = nullToZero(order.getOrderAmount());
        }
        BigDecimal refundedTotal = nullToZero(event.getRefundedAmountTotal());
        BigDecimal remain = payAmount.subtract(refundedTotal);
        if (remain.compareTo(BigDecimal.ZERO) < 0) {
            remain = BigDecimal.ZERO;
        }

        List<DistCommissionItem> pendingItems = commissionItemMapper.selectList(
            Wrappers.<DistCommissionItem>lambdaQuery()
                .eq(DistCommissionItem::getOrderNo, event.getOrderNo())
                .eq(DistCommissionItem::getStatus, DistCommissionStatus.PENDING.getCode())
        );
        if (pendingItems == null || pendingItems.isEmpty()) {
            log.info("AFTER_SALE_REFUNDED 无待结算明细 orderNo={}", event.getOrderNo());
            // 头表基数仍同步为剩余，便于对账
            order.setOrderAmount(remain);
            commissionOrderMapper.updateById(order);
            return;
        }

        Date now = new Date();
        int updated = 0;
        int cancelled = 0;
        for (DistCommissionItem item : pendingItems) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                item.setAmount(BigDecimal.ZERO);
                item.setStatus(DistCommissionStatus.CANCELLED.getCode());
                item.setCancelTime(now);
                item.setCancelReason(CANCEL_REASON_REFUND);
                cancelled++;
            } else {
                BigDecimal rate = item.getRate() == null ? BigDecimal.ZERO : item.getRate();
                BigDecimal amount = remain.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                item.setAmount(amount);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    item.setStatus(DistCommissionStatus.CANCELLED.getCode());
                    item.setCancelTime(now);
                    item.setCancelReason(CANCEL_REASON_REFUND);
                    cancelled++;
                } else {
                    updated++;
                }
            }
            commissionItemMapper.updateById(item);
        }

        order.setOrderAmount(remain);
        commissionOrderMapper.updateById(order);
        log.info("AFTER_SALE_REFUNDED 待结算重算完成 orderNo={} afterSaleNo={} remain={} updated={} cancelled={}",
            event.getOrderNo(), event.getAfterSaleNo(), remain, updated, cancelled);
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
