package org.dromara.order.util;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.mapper.OrderItemMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单行售后占用维护（申请中 / 已退成功）。与售后状态变更同事务调用。
 */
@Component
@RequiredArgsConstructor
public class OrderItemRefundOccupyWriter {

    private final OrderItemMapper orderItemMapper;

    /** 创建售后：增加申请中占用 */
    public void addRefunding(List<AfterSaleItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (AfterSaleItem item : items) {
            int qty = nullToZero(item.getReturnQuantity());
            BigDecimal amount = nullToZero(item.getRefundAmount());
            if (qty <= 0) {
                continue;
            }
            int rows = orderItemMapper.addRefunding(item.getOrderItemId(), qty, amount);
            if (rows <= 0) {
                throw new ServiceException("更新订单行申请中占用失败 orderItemId=" + item.getOrderItemId());
            }
        }
    }

    /** 撤销 / 拒绝：释放申请中占用 */
    public void subRefunding(List<AfterSaleItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (AfterSaleItem item : items) {
            int qty = nullToZero(item.getReturnQuantity());
            BigDecimal amount = nullToZero(item.getRefundAmount());
            if (qty <= 0) {
                continue;
            }
            int rows = orderItemMapper.subRefunding(item.getOrderItemId(), qty, amount);
            if (rows <= 0) {
                throw new ServiceException("释放订单行申请中占用失败 orderItemId=" + item.getOrderItemId());
            }
        }
    }

    /** 退款成功：申请中 → 已退成功 */
    public void moveRefundingToRefunded(List<AfterSaleItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (AfterSaleItem item : items) {
            int qty = nullToZero(item.getReturnQuantity());
            BigDecimal amount = nullToZero(item.getRefundAmount());
            if (qty <= 0) {
                continue;
            }
            int rows = orderItemMapper.moveRefundingToRefunded(item.getOrderItemId(), qty, amount);
            if (rows <= 0) {
                throw new ServiceException("订单行占用转已退失败 orderItemId=" + item.getOrderItemId());
            }
        }
    }

    /** 赠品系统收回等：直接记已退成功（无申请中） */
    public void addRefunded(List<AfterSaleItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (AfterSaleItem item : items) {
            int qty = nullToZero(item.getReturnQuantity());
            BigDecimal amount = nullToZero(item.getRefundAmount());
            if (qty <= 0) {
                continue;
            }
            int rows = orderItemMapper.addRefunded(item.getOrderItemId(), qty, amount);
            if (rows <= 0) {
                throw new ServiceException("更新订单行已退占用失败 orderItemId=" + item.getOrderItemId());
            }
        }
    }

    private static int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
