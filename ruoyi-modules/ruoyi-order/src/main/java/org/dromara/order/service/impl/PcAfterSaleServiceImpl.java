package org.dromara.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.order.constant.OrderCacheKeys;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.bo.PcAfterSaleApplyBo;
import org.dromara.order.domain.bo.PcAfterSaleApplyItemBo;
import org.dromara.order.domain.bo.PcAfterSaleReturnExpressBo;
import org.dromara.order.domain.vo.AfterSaleItemVo;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.vo.PcAfterSaleApplyResultVo;
import org.dromara.order.domain.vo.PcAfterSaleDetailVo;
import org.dromara.order.mapper.AfterSaleItemMapper;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IPcAfterSaleService;
import org.dromara.order.util.OrderItemRefundOccupyWriter;
import org.dromara.order.util.OrderPayStatusHelper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 买家售后：申请 / 列表 / 详情 / 填快递 / 撤销
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PcAfterSaleServiceImpl implements IPcAfterSaleService {

    private static final Long SERVICE_REFUND_ONLY = 1L;
    private static final Long SERVICE_RETURN_REFUND = 2L;
    private static final Long APPLY_SOURCE_BUYER = 1L;
    private static final Long STATUS_PENDING_AUDIT = 10L;
    private static final Long STATUS_WAIT_RETURN = 20L;
    private static final Long STATUS_WAIT_INSPECT = 30L;
    private static final Long STATUS_REVOKED = 99L;

    private static final Long ORDER_STATUS_PENDING_SHIP = 20L;
    private static final Long ORDER_STATUS_PENDING_RECEIVE = 30L;
    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 5L;

    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleItemMapper afterSaleItemMapper;
    private final OrderItemRefundOccupyWriter orderItemRefundOccupyWriter;

    @Lazy
    @Autowired
    private PcAfterSaleServiceImpl self;

    @Override
    public PcAfterSaleApplyResultVo apply(PcAfterSaleApplyBo bo, String tenantId, Long userinfoId) {
        if (bo == null || StringUtils.isBlank(tenantId) || userinfoId == null) {
            throw new ServiceException("参数不完整");
        }
        String orderNo = bo.getOrderNo();
        RLock lock = redissonClient.getLock(OrderCacheKeys.orderLifecycleLock(orderNo));
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new ServiceException("正在处理售后申请，请勿重复提交");
            }
            try {
                return self.doApply(bo, tenantId, userinfoId);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("申请处理中断，请稍后重试");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PcAfterSaleApplyResultVo doApply(PcAfterSaleApplyBo bo, String tenantId, Long userinfoId) {
        // 事务内锁订单行，与确认收货互斥
        Order order = findOrderForUpdate(tenantId, bo.getOrderNo());
        // 必须是本人
        if (order == null || !userinfoId.equals(order.getUserinfoId())) {
            throw new ServiceException("订单不存在或无权操作");
        }
        OrderPayStatusHelper.requirePaidOrPartialRefund(order.getPayStatus());
        validateServiceTypeGate(bo.getServiceType(), order.getOrderStatus());

        List<PcAfterSaleApplyItemBo> applyItems = bo.getItems();
        List<Long> orderItemIds = applyItems.stream().map(PcAfterSaleApplyItemBo::getOrderItemId).toList();
        // 查出这些明细，确认都属于这张订单
        List<OrderItem> orderItems = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
            .eq(OrderItem::getTenantId, tenantId)
            .eq(OrderItem::getOrderNo, bo.getOrderNo())
            .in(OrderItem::getId, orderItemIds));
        if (orderItems.size() != applyItems.size()) {
            throw new ServiceException("售后商品不属于该订单或不存在");
        }

        // 明细id->这一行本身
        Map<Long, OrderItem> itemMap = orderItems.stream()
            .collect(Collectors.toMap(OrderItem::getId, i -> i, (a, b) -> a));

        List<AfterSaleItem> afterItems = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (PcAfterSaleApplyItemBo applyItem : applyItems) {
            Long orderItemId = applyItem.getOrderItemId();
            // 申请件数
            int askQty = applyItem.getReturnQuantity();
            OrderItem oi = itemMap.get(orderItemId);
            if (oi.getIsGift() != null && oi.getIsGift() == 1L) {
                throw new ServiceException("赠品不能单独申请售后，主品退完后系统自动收回");
            }
            int buyQty = oi.getQuantity().intValue();
            int refundedQty = nullToZero(oi.getRefundedQty());
            int refundingQty = nullToZero(oi.getRefundingQty());
            // 可退 = 购买数 - 已退成功 - 在途占用
            int available = buyQty - refundedQty - refundingQty;
            if (askQty > available) {
                throw new ServiceException("商品【"
                    + StringUtils.blankToDefault(oi.getGoodsTitle(), String.valueOf(oi.getSkuId()))
                    + "】可退数量不足，剩余可退 " + Math.max(available, 0));
            }
            BigDecimal lineRefund = calcLineRefund(oi, askQty, available,
                nullToZero(oi.getRefundedAmount()), nullToZero(oi.getRefundingAmount()));
            AfterSaleItem asi = new AfterSaleItem();
            asi.setTenantId(tenantId);
            asi.setOrderNo(bo.getOrderNo());
            asi.setOrderItemId(orderItemId);
            asi.setSkuId(oi.getSkuId());
            asi.setSkuCode(oi.getSkuCode());
            asi.setGoodsTitle(oi.getGoodsTitle());
            asi.setSpecText(oi.getSpecText());
            asi.setCoverImage(oi.getCoverImage());
            asi.setReturnQuantity(askQty);
            asi.setRefundPrice(oi.getSharePrice());
            asi.setRefundAmount(lineRefund);
            afterItems.add(asi);
            totalRefund = totalRefund.add(lineRefund);
        }

        String afterSaleNo = generateAfterSaleNo(tenantId);
        AfterSale afterSale = new AfterSale();
        afterSale.setTenantId(tenantId);
        afterSale.setAfterSaleNo(afterSaleNo);
        afterSale.setOrderNo(bo.getOrderNo());
        afterSale.setUserinfoId(userinfoId);
        afterSale.setMemberBcode(order.getMemberBcode());
        afterSale.setMemberCode(order.getMemberCode());
        afterSale.setServiceType(bo.getServiceType());
        afterSale.setApplySource(APPLY_SOURCE_BUYER);
        afterSale.setStatus(STATUS_PENDING_AUDIT);
        afterSale.setRefundAmount(totalRefund);
        afterSale.setApplyReason(bo.getApplyReason());
        afterSale.setProofImgs(bo.getProofImgs());
        afterSale.setApplyRemark(bo.getApplyRemark());
        afterSaleMapper.insert(afterSale);
        for (AfterSaleItem asi : afterItems) {
            asi.setAfterSaleNo(afterSaleNo);
            afterSaleItemMapper.insert(asi);
        }
        orderItemRefundOccupyWriter.addRefunding(afterItems);

        log.info("买家申请售后成功 afterSaleNo={} orderNo={} refundAmount={}", afterSaleNo, bo.getOrderNo(), totalRefund);

        PcAfterSaleApplyResultVo result = new PcAfterSaleApplyResultVo();
        result.setAfterSaleNo(afterSaleNo);
        result.setRefundAmount(totalRefund);
        return result;
    }

    @Override
    public TableDataInfo<AfterSaleVo> listMine(String tenantId, Long userinfoId, PageQuery pageQuery) {
        Page<AfterSaleVo> page = afterSaleMapper.selectVoPage(pageQuery.build(),
            Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getTenantId, tenantId)
                .eq(AfterSale::getUserinfoId, userinfoId)
                .orderByDesc(AfterSale::getCreateTime));
        return TableDataInfo.build(page);
    }

    @Override
    public PcAfterSaleDetailVo detail(String afterSaleNo, String tenantId, Long userinfoId) {
        if (StringUtils.isBlank(afterSaleNo)) {
            throw new ServiceException("售后单号不能为空");
        }
        AfterSale afterSale = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getAfterSaleNo, afterSaleNo)
            .last("LIMIT 1"));
        if (afterSale == null || !userinfoId.equals(afterSale.getUserinfoId())) {
            throw new ServiceException("售后单不存在或无权查看");
        }
        PcAfterSaleDetailVo detail = new PcAfterSaleDetailVo();
        BeanUtils.copyProperties(afterSale, detail);
        List<AfterSaleItemVo> items = afterSaleItemMapper.selectVoList(Wrappers.<AfterSaleItem>lambdaQuery()
            .eq(AfterSaleItem::getTenantId, tenantId)
            .eq(AfterSaleItem::getAfterSaleNo, afterSaleNo));
        detail.setItems(items);
        return detail;
    }

    @Override
    public void fillReturnExpress(PcAfterSaleReturnExpressBo bo, String tenantId, Long userinfoId) {
        if (bo == null || StringUtils.isBlank(tenantId) || userinfoId == null) {
            throw new ServiceException("参数不完整");
        }
        doFillReturnExpress(bo, tenantId, userinfoId);
    }

    private void doFillReturnExpress(PcAfterSaleReturnExpressBo bo, String tenantId, Long userinfoId) {
        AfterSale afterSale = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getAfterSaleNo, bo.getAfterSaleNo())
            .last("LIMIT 1"));
        if (afterSale == null || !userinfoId.equals(afterSale.getUserinfoId())) {
            throw new ServiceException("售后单不存在或无权操作");
        }
        if (!SERVICE_RETURN_REFUND.equals(afterSale.getServiceType())) {
            throw new ServiceException("仅退货退款单需要填写快递");
        }
        if (!STATUS_WAIT_RETURN.equals(afterSale.getStatus())) {
            throw new ServiceException("当前状态不可填写退货快递");
        }
        String company = StringUtils.trim(bo.getReturnExpressCompany());
        String expressNo = StringUtils.trim(bo.getReturnExpressNo());
        if (StringUtils.isBlank(company) || StringUtils.isBlank(expressNo)) {
            throw new ServiceException("快递公司与单号不能为空");
        }

        Date now = new Date();
        int rows = afterSaleMapper.fillReturnExpressCas(
            tenantId, bo.getAfterSaleNo(),
            STATUS_WAIT_RETURN, STATUS_WAIT_INSPECT,
            company, expressNo, now);
        if (rows <= 0) {
            throw new ServiceException("提交失败，单据可能已填写过快递或状态已变更");
        }
        log.info("买家填写退货快递 afterSaleNo={} expressNo={}", bo.getAfterSaleNo(), expressNo);
    }

    @Override
    public void revoke(String afterSaleNo, String tenantId, Long userinfoId) {
        if (StringUtils.isBlank(afterSaleNo) || StringUtils.isBlank(tenantId) || userinfoId == null) {
            throw new ServiceException("参数不完整");
        }
        doRevoke(afterSaleNo, tenantId, userinfoId);
    }

    private void doRevoke(String afterSaleNo, String tenantId, Long userinfoId) {
        AfterSale afterSale = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getAfterSaleNo, afterSaleNo)
            .last("LIMIT 1"));
        if (afterSale == null || !userinfoId.equals(afterSale.getUserinfoId())) {
            throw new ServiceException("售后单不存在或无权操作");
        }
        Long status = afterSale.getStatus();
        // 只要待审核或待买家退货能撤销申请
        if (!STATUS_PENDING_AUDIT.equals(status) && !STATUS_WAIT_RETURN.equals(status)) {
            throw new ServiceException("当前售后状态不可撤销");
        }
        Date now = new Date();
        // 只有待审核或待买家退货才更新为已撤销
        int rows = afterSaleMapper.revokeByStatusCas(tenantId, afterSaleNo, status, STATUS_REVOKED, now);
        if (rows <= 0) {
            throw new ServiceException("撤销失败，单据可能已被处理");
        }
        List<AfterSaleItem> items = afterSaleItemMapper.selectList(Wrappers.<AfterSaleItem>lambdaQuery()
            .eq(AfterSaleItem::getTenantId, tenantId)
            .eq(AfterSaleItem::getAfterSaleNo, afterSaleNo));
        orderItemRefundOccupyWriter.subRefunding(items);
        log.info("买家撤销售后 afterSaleNo={} orderNo={} fromStatus={}", afterSaleNo, afterSale.getOrderNo(), status);
    }

    private void validateServiceTypeGate(Long serviceType, Long orderStatus) {
        if (SERVICE_REFUND_ONLY.equals(serviceType)) {
            if (!ORDER_STATUS_PENDING_SHIP.equals(orderStatus)) {
                throw new ServiceException("仅待发货订单可申请仅退款");
            }
            return;
        }
        if (SERVICE_RETURN_REFUND.equals(serviceType)) {
            if (!ORDER_STATUS_PENDING_RECEIVE.equals(orderStatus)) {
                throw new ServiceException("仅待收货订单可申请退货退款");
            }
            return;
        }
        throw new ServiceException("不支持的售后类型");
    }

    /**
     * 计算本行本次申请退款金额。
     *
     * @param oi           订单明细
     * @param askQty       本次申请件数
     * @param available    本行当前可退件数（购买 − 已退成功 − 在途）
     * @param successAmt   本行已退款成功金额
     * @param inTransitAmt 本行在途售后占用金额
     */
    private BigDecimal calcLineRefund(OrderItem oi, int askQty, int available, BigDecimal successAmt, BigDecimal inTransitAmt) {
        // 退光剩余可退件数
        if (askQty == available) {
            return oi.getRealPayAmount()
                .subtract(successAmt)
                .subtract(inTransitAmt);
        }
        // 部分退：按分摊单价 × 件数
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

    private Order findOrderForUpdate(String tenantId, String orderNo) {
        return orderMapper.selectOne(Wrappers.<Order>lambdaQuery()
            .eq(Order::getTenantId, tenantId)
            .eq(Order::getOrderNo, orderNo)
            .eq(Order::getDelFlag, "0")
            .last("LIMIT 1 FOR UPDATE"));
    }

    private static int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
