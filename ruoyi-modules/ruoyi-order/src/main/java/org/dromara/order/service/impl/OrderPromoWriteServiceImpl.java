package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.domain.OrderPromo;
import org.dromara.order.domain.OrderPromoItem;
import org.dromara.order.domain.bo.ExclusivePromoRef;
import org.dromara.order.domain.bo.ReverseAgg;
import org.dromara.order.domain.bo.SavedOrderItemLine;
import org.dromara.order.domain.bo.SharedPromoAssetsBo;
import org.dromara.order.domain.vo.PcCheckoutAvailableCouponVo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;
import org.dromara.order.domain.vo.PcCheckoutCouponSelectorVo;
import org.dromara.order.domain.vo.PcCheckoutPriceSummaryVo;
import org.dromara.order.enums.OrderPromoEnums.PromoStatus;
import org.dromara.order.enums.OrderPromoEnums.RecordType;
import org.dromara.order.enums.OrderPromoEnums.SourceType;
import org.dromara.order.mapper.OrderPromoItemMapper;
import org.dromara.order.mapper.OrderPromoMapper;
import org.dromara.order.service.IOrderPromoWriteService;
import org.dromara.order.util.PromoReversalCalculator;
import org.dromara.order.util.PromoReversalCalculator.ReversalCalculation;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单营销主/子表写入：下单正向、售后/取消逆向、共享资产 CAS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPromoWriteServiceImpl implements IOrderPromoWriteService {

    private static final String TYPE_FULL_GIFT = "FULL_GIFT";
    private static final String TYPE_SECKILL = "SECKILL";
    private static final String TYPE_SPECIAL = "SPECIAL_PRICE";

    private final OrderPromoMapper orderPromoMapper;
    private final OrderPromoItemMapper orderPromoItemMapper;



    @Override
    public void writeOnSubmit(String tenantId, String channelCode, String parentOrderNo,
                              Long exclusiveActivityId, String exclusivePromoType,
                              PcCheckoutConfirmVo priceVO, List<SavedOrderItemLine> lines) {
        if (StringUtils.isAnyBlank(tenantId, parentOrderNo) || CollUtil.isEmpty(lines)) {
            return;
        }
        writeShopPromos(tenantId, channelCode, parentOrderNo, lines);
        writeCrossPromo(tenantId, channelCode, parentOrderNo, priceVO, lines);
        writeCouponPromo(tenantId, channelCode, parentOrderNo, priceVO, lines);
        writeExclusiveItems(tenantId, channelCode, parentOrderNo, exclusiveActivityId, exclusivePromoType, lines);
    }

    private void writeShopPromos(String tenantId, String channelCode, String parentOrderNo, List<SavedOrderItemLine> lines) {
        for (List<SavedOrderItemLine> groupLines : collectShopDiscountGroups(lines).values()) {
            SavedOrderItemLine first = groupLines.get(0);
            BigDecimal platformRate = first.getSelectedShopPlatformShareRate();
            BigDecimal totalDisc = sumLineDiscount(groupLines, SavedOrderItemLine::getShopDiscountAmount);
            BigDecimal totalPlatform = calcPlatformShare(totalDisc, platformRate);
            BigDecimal totalMerchant = totalDisc.subtract(totalPlatform);
            OrderPromo main = newMain(tenantId, channelCode, parentOrderNo, SourceType.SHOP,
                first.getSelectedPromoType(), first.getSelectedShopActivityId(), 0L,
                first.getSelectedActivityTitle(), totalDisc, totalPlatform, totalMerchant);
            orderPromoMapper.insert(main);
            insertItemsWithLastLineShareAbsorb(tenantId, parentOrderNo, groupLines, main.getId(),
                SourceType.SHOP.getCode(), platformRate, totalPlatform, SavedOrderItemLine::getShopDiscountAmount);
        }
        writeShopFullGiftPromos(tenantId, channelCode, parentOrderNo, lines);
    }

    private void writeShopFullGiftPromos(String tenantId, String channelCode, String parentOrderNo, List<SavedOrderItemLine> lines) {
        Map<Long, List<SavedOrderItemLine>> giftsByActivity = new LinkedHashMap<>();
        for (SavedOrderItemLine line : lines) {
            if (!line.isGift() || line.getPromoActivityId() == null) {
                continue;
            }
            giftsByActivity.computeIfAbsent(line.getPromoActivityId(), k -> new ArrayList<>()).add(line);
        }
        for (Map.Entry<Long, List<SavedOrderItemLine>> entry : giftsByActivity.entrySet()) {
            List<SavedOrderItemLine> gifts = entry.getValue();
            SavedOrderItemLine first = gifts.get(0);
            OrderPromo main = newMain(tenantId, channelCode, parentOrderNo, SourceType.SHOP,
                TYPE_FULL_GIFT, entry.getKey(), 0L, first.getSelectedActivityTitle(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            orderPromoMapper.insert(main);
            for (SavedOrderItemLine gift : gifts) {
                insertItem(tenantId, parentOrderNo, gift, main.getId(), SourceType.SHOP.getCode(),
                    gift.getQuantity(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        }
    }

    private void writeCrossPromo(String tenantId, String channelCode, String parentOrderNo,
                                 PcCheckoutConfirmVo priceVO, List<SavedOrderItemLine> lines) {
        List<SavedOrderItemLine> hitLines = lines.stream()
            .filter(l -> !l.isGift() && nullToZero(l.getCrossDiscountAmount()).compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(hitLines)) {
            return;
        }
        PcCheckoutPriceSummaryVo summary = Optional.ofNullable(priceVO)
            .map(PcCheckoutConfirmVo::getPriceSummary).orElse(null);
        Long activityId = summary == null ? null : summary.getSelectedCrossActivityId();
        if (activityId == null) {
            throw new ServiceException("跨店优惠活动已失效，请返回重新结算");
        }
        BigDecimal platformRate = summary.getSelectedCrossPlatformShareRate();
        BigDecimal totalDisc = sumLineDiscount(hitLines, SavedOrderItemLine::getCrossDiscountAmount);
        BigDecimal totalPlatform = calcPlatformShare(totalDisc, platformRate);
        BigDecimal totalMerchant = totalDisc.subtract(totalPlatform);
        OrderPromo main = newMain(tenantId, channelCode, parentOrderNo, SourceType.CROSS,
            summary.getSelectedCrossPromoType(), activityId, 0L,
            summary.getSelectedCrossActivityTitle(), totalDisc, totalPlatform, totalMerchant);
        orderPromoMapper.insert(main);
        insertItemsWithLastLineShareAbsorb(tenantId, parentOrderNo, hitLines, main.getId(),
            SourceType.CROSS.getCode(), platformRate, totalPlatform, SavedOrderItemLine::getCrossDiscountAmount);
    }

    private void writeCouponPromo(String tenantId, String channelCode, String parentOrderNo,
                                  PcCheckoutConfirmVo priceVO, List<SavedOrderItemLine> lines) {
        List<SavedOrderItemLine> hitLines = lines.stream()
            .filter(l -> !l.isGift() && nullToZero(l.getCouponDiscountAmount()).compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(hitLines)) {
            return;
        }
        PcCheckoutCouponSelectorVo selector = Optional.ofNullable(priceVO)
            .map(PcCheckoutConfirmVo::getCouponSelector).orElse(null);
        if (selector == null || selector.getSelectedUserCouponId() == null) {
            throw new ServiceException("优惠券信息已失效，请返回重新结算");
        }
        PcCheckoutAvailableCouponVo selected = Optional.ofNullable(selector.getAvailableCoupons())
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(c -> selector.getSelectedUserCouponId().equals(c.getUserCouponId()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("优惠券信息已失效，请返回重新结算"));
        if (selected.getTemplateId() == null) {
            throw new ServiceException("优惠券模板信息缺失，请返回重新结算");
        }
        BigDecimal platformRate = selected.getPlatformShareRate();
        BigDecimal totalDisc = sumLineDiscount(hitLines, SavedOrderItemLine::getCouponDiscountAmount);
        BigDecimal totalPlatform = calcPlatformShare(totalDisc, platformRate);
        BigDecimal totalMerchant = totalDisc.subtract(totalPlatform);
        OrderPromo main = newMain(tenantId, channelCode, parentOrderNo, SourceType.COUPON,
            selected.getCouponType(), selected.getTemplateId(), selected.getUserCouponId(),
            selected.getTitle(), totalDisc, totalPlatform, totalMerchant);
        orderPromoMapper.insert(main);
        insertItemsWithLastLineShareAbsorb(tenantId, parentOrderNo, hitLines, main.getId(),
            SourceType.COUPON.getCode(), platformRate, totalPlatform, SavedOrderItemLine::getCouponDiscountAmount);
    }

    private void writeExclusiveItems(String tenantId, String channelCode, String parentOrderNo,
                                     Long exclusiveActivityId, String exclusivePromoType,
                                     List<SavedOrderItemLine> lines) {
        if (exclusiveActivityId == null) {
            return;
        }
        String promoType = normalizeExclusive(exclusivePromoType);
        if (promoType == null) {
            return;
        }
        List<SavedOrderItemLine> hitLines = lines.stream()
            .filter(l -> !l.isGift() && exclusiveActivityId.equals(l.getPromoActivityId()))
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(hitLines)) {
            return;
        }
        SavedOrderItemLine first = hitLines.get(0);
        OrderPromo main = newMain(tenantId, channelCode, parentOrderNo, SourceType.EXCLUSIVE,
            promoType, exclusiveActivityId, 0L, first.getSelectedActivityTitle(),
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        orderPromoMapper.insert(main);
        for (SavedOrderItemLine line : hitLines) {
            insertItem(tenantId, parentOrderNo, line, main.getId(), SourceType.EXCLUSIVE.getCode(),
                line.getQuantity(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }



    /**
     * 售后退款成功后，按本次退款件数写入促销逆向明细。
     * 下单时记了优惠（正向），退多少就冲多少
     * @param tenantId           租户
     * @param parentOrderNo      父单号
     * @param afterSaleNo        售后单号
     * @param orderItemRefundQty 订单明细ID → 本次退款件数
     */
    @Override
    public void writeReverseOnRefund(String tenantId, String parentOrderNo, String afterSaleNo, Map<Long, Integer> orderItemRefundQty) {
        Set<Long> orderItemIds = orderItemRefundQty.keySet();
        // 获取订单行上的正向促销明细
        List<OrderPromoItem> forwards = orderPromoItemMapper.selectList(Wrappers.<OrderPromoItem>lambdaQuery()
            .eq(OrderPromoItem::getTenantId, tenantId)
            .in(OrderPromoItem::getOrderItemId, orderItemIds)
            .eq(OrderPromoItem::getRecordType, RecordType.FORWARD.getCode()));
        if (CollUtil.isEmpty(forwards)) {
            return;
        }
        // 历史已冲汇总，用来算剩余可冲件数
        Map<String, ReverseAgg> historyReverses = loadReversalAggMap(tenantId, orderItemIds);
        for (OrderPromoItem fwd : forwards) {
            int requestQty = orderItemRefundQty.get(fwd.getOrderItemId());
            int buyQty = fwd.getQuantity();
            ReverseAgg done = historyReverses.getOrDefault(
                buildAggKey(fwd.getOrderItemId(), fwd.getOrderPromoId(), fwd.getPromoSourceType()),
                new ReverseAgg());
            // 剩余可冲 = 正向件数 − 已冲件数
            int remainQty = buyQty - done.getQty();
            insertReverseIfNeeded(fwd, done, requestQty, remainQty, buyQty, afterSaleNo);
        }
    }

    @Override
    public void writeReverseOnCancel(String tenantId, String parentOrderNo, String orderNo) {
        if (StringUtils.isAnyBlank(tenantId, orderNo)) {
            return;
        }
        // 查正向促销明细
        List<OrderPromoItem> forwards = orderPromoItemMapper.selectList(Wrappers.<OrderPromoItem>lambdaQuery()
            .eq(OrderPromoItem::getTenantId, tenantId)
            .eq(OrderPromoItem::getOrderNo, orderNo)
            .eq(OrderPromoItem::getRecordType, RecordType.FORWARD.getCode()));
        if (CollUtil.isEmpty(forwards)) {
            return;
        }
        Set<Long> orderItemIds = forwards.stream()
            .map(OrderPromoItem::getOrderItemId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, ReverseAgg> historyReverses = loadReversalAggMap(tenantId, orderItemIds);
        reverseRemainingForwards(forwards, historyReverses);
    }

    /**
     * 取消专用冲销：把每条正向促销还没冲完的件数一次性冲掉。
     */
    private void reverseRemainingForwards(List<OrderPromoItem> forwards, Map<String, ReverseAgg> historyReverses) {
        for (OrderPromoItem fwd : forwards) {
            int buyQty = fwd.getQuantity();
            ReverseAgg done = historyReverses.getOrDefault(
                buildAggKey(fwd.getOrderItemId(), fwd.getOrderPromoId(), fwd.getPromoSourceType()),
                new ReverseAgg());
            // 剩余可冲件数 = 正向减去已冲去的
            int remainQty = buyQty - done.getQty();
            if (remainQty <= 0) {
                continue;
            }
            insertReverseIfNeeded(fwd, done, remainQty, remainQty, buyQty, "");
        }
    }

    /**
     * 对一条正向促销明细算出这次该冲几件、冲多少钱，再插入逆向行。
     * @param fwd        待冲销的正向促销明细
     * @param done       该正向维度历史已冲汇总
     * @param requestQty 本次申请冲正件数
     * @param remainQty  该正向还剩可冲件数
     * @param buyQty     正向下单件数，分摊金额用
     * @param refNo      业务参考号：退款为售后单号，取消为空串
     */
    private void insertReverseIfNeeded(OrderPromoItem fwd, ReverseAgg done, int requestQty, int remainQty, int buyQty, String refNo) {
        ReversalCalculation calc = PromoReversalCalculator.calculate(fwd, done, requestQty, remainQty, buyQty);
        insertReverseQuietly(fwd, refNo, calc.getQuantity(), calc.getDiscount(), calc.getPlatform(), calc.getMerchant());
    }


    @Override
    public int casReleaseSharedAssets(String tenantId, String parentOrderNo) {
        if (StringUtils.isAnyBlank(tenantId, parentOrderNo)) {
            return 0;
        }
        return orderPromoMapper.update(null, Wrappers.<OrderPromo>lambdaUpdate()
            .set(OrderPromo::getStatus, PromoStatus.RELEASED.getCode())
            .eq(OrderPromo::getTenantId, tenantId)
            .eq(OrderPromo::getParentOrderNo, parentOrderNo)
            .in(OrderPromo::getPromoSourceType, SourceType.CROSS.getCode(), SourceType.COUPON.getCode())
            .eq(OrderPromo::getStatus, PromoStatus.ACTIVE.getCode()));
    }

    @Override
    public SharedPromoAssetsBo loadSharedAssets(String tenantId, String parentOrderNo) {
        SharedPromoAssetsBo.SharedPromoAssetsBoBuilder builder = SharedPromoAssetsBo.builder();
        if (StringUtils.isAnyBlank(tenantId, parentOrderNo)) {
            return builder.build();
        }
        // 查父单下全部促销主表记录
        List<OrderPromo> mains = orderPromoMapper.selectList(Wrappers.<OrderPromo>lambdaQuery()
            .eq(OrderPromo::getTenantId, tenantId)
            .eq(OrderPromo::getParentOrderNo, parentOrderNo));
        if (CollUtil.isEmpty(mains)) {
            return builder.build();
        }
        Long userCouponId = null;
        Long crossActivityId = null;
        BigDecimal crossDiscount = BigDecimal.ZERO;
        LinkedHashSet<Long> joinIds = new LinkedHashSet<>();
        for (OrderPromo main : mains) {
            Long source = main.getPromoSourceType();
            if (Objects.equals(source, SourceType.COUPON.getCode()) && userCouponId == null) {
                userCouponId = main.getUserCouponId();
            }
            if (Objects.equals(source, SourceType.CROSS.getCode()) && main.getActivityId() != null) {
                crossActivityId = main.getActivityId();
                crossDiscount = crossDiscount.add(nullToZero(main.getDiscountAmount()));
            }
            if ((Objects.equals(source, SourceType.SHOP.getCode()) || Objects.equals(source, SourceType.CROSS.getCode()))
                && main.getActivityId() != null && main.getActivityId() > 0) {
                joinIds.add(main.getActivityId());
            }
        }
        builder.userCouponId(userCouponId);
        if (crossActivityId != null && crossDiscount.compareTo(BigDecimal.ZERO) > 0) {
            builder.crossActivityId(crossActivityId);
            builder.crossDiscountAmount(scale(crossDiscount));
        }
        builder.promoJoinActivityIds(new ArrayList<>(joinIds));
        return builder.build();
    }

    /**
     * 根据订单行 id，查出这行下单时用的专享活动 id
     */
    @Override
    public Map<Long, ExclusivePromoRef> loadExclusivePromos(String tenantId, List<Long> orderItemIds) {
        if (StringUtils.isBlank(tenantId) || CollUtil.isEmpty(orderItemIds)) {
            return Collections.emptyMap();
        }
        // 查专享明细表
        List<OrderPromoItem> rows = orderPromoItemMapper.selectList(Wrappers.<OrderPromoItem>lambdaQuery()
            .eq(OrderPromoItem::getTenantId, tenantId)
            .eq(OrderPromoItem::getPromoSourceType, SourceType.EXCLUSIVE.getCode())
            .eq(OrderPromoItem::getRecordType, RecordType.FORWARD.getCode())
            .in(OrderPromoItem::getOrderItemId, orderItemIds)
            .gt(OrderPromoItem::getOrderPromoId, 0));
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }

        Set<Long> promoIds = rows.stream()
            .map(OrderPromoItem::getOrderPromoId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, OrderPromo> mainMap = CollUtil.isEmpty(promoIds) ? Collections.emptyMap()
            : orderPromoMapper.selectList(Wrappers.<OrderPromo>lambdaQuery()
                .eq(OrderPromo::getTenantId, tenantId)
                .in(OrderPromo::getId, promoIds)
                .eq(OrderPromo::getPromoSourceType, SourceType.EXCLUSIVE.getCode()))
            .stream()
            .collect(Collectors.toMap(OrderPromo::getId, p -> p, (a, b) -> a));
        Map<Long, ExclusivePromoRef> map = new HashMap<>();
        for (OrderPromoItem row : rows) {
            OrderPromo main = mainMap.get(row.getOrderPromoId());
            if (main == null || main.getActivityId() == null || main.getActivityId() <= 0) {
                continue;
            }
            map.putIfAbsent(row.getOrderItemId(), ExclusivePromoRef.builder()
                .orderItemId(row.getOrderItemId())
                .activityId(main.getActivityId())
                .promoType(normalizeExclusive(main.getPromoType()))
                .build());
        }
        return map;
    }



    private void insertItem(String tenantId, String parentOrderNo, SavedOrderItemLine line,
                            Long orderPromoId, Long source,
                            int quantity, BigDecimal discount, BigDecimal platform, BigDecimal merchant) {
        OrderPromoItem item = new OrderPromoItem();
        item.setTenantId(tenantId);
        item.setParentOrderNo(parentOrderNo);
        item.setOrderNo(line.getOrderNo());
        item.setOrderItemId(line.getOrderItemId());
        item.setOrderPromoId(orderPromoId);
        item.setAfterSaleNo("");
        item.setMemberCode(line.getMemberCode());
        item.setSkuId(line.getSkuId());
        item.setPromoSourceType(source);
        item.setRecordType(RecordType.FORWARD.getCode());
        item.setQuantity(quantity);
        item.setDiscountAmount(discount);
        item.setPlatformShareAmount(platform);
        item.setMerchantShareAmount(merchant);
        orderPromoItemMapper.insert(item);
    }

    private void insertReverseQuietly(OrderPromoItem fwd, String afterSaleNo, int qty, BigDecimal disc, BigDecimal platform, BigDecimal merchant) {
        OrderPromoItem rev = new OrderPromoItem();
        rev.setTenantId(fwd.getTenantId());
        rev.setParentOrderNo(fwd.getParentOrderNo());
        rev.setOrderNo(fwd.getOrderNo());
        rev.setOrderItemId(fwd.getOrderItemId());
        rev.setOrderPromoId(fwd.getOrderPromoId());
        rev.setAfterSaleNo(afterSaleNo);
        rev.setMemberCode(fwd.getMemberCode());
        rev.setSkuId(fwd.getSkuId());
        rev.setPromoSourceType(fwd.getPromoSourceType());
        rev.setRecordType(RecordType.REVERSE.getCode());
        rev.setQuantity(qty);
        rev.setDiscountAmount(disc);
        rev.setPlatformShareAmount(platform);
        rev.setMerchantShareAmount(merchant);
        try {
            orderPromoItemMapper.insert(rev);
        } catch (DuplicateKeyException ex) {
            log.info("营销逆向流水已存在，幂等跳过 afterSaleNo={} orderItemId={} promoId={} source={}", afterSaleNo, fwd.getOrderItemId(), fwd.getOrderPromoId(), fwd.getPromoSourceType());
        }
    }

    /**
     * 把指定订单行上已经存在的逆向流水聚合成一张表，后面算「还能冲多少」。
     */
    private Map<String, ReverseAgg> loadReversalAggMap(String tenantId, Set<Long> orderItemIds) {
        if (CollUtil.isEmpty(orderItemIds)) {
            return Collections.emptyMap();
        }
        //  查已有逆向
        List<OrderPromoItem> reverses = orderPromoItemMapper.selectList(Wrappers.<OrderPromoItem>lambdaQuery()
            .eq(OrderPromoItem::getTenantId, tenantId)
            .in(OrderPromoItem::getOrderItemId, orderItemIds)
            .eq(OrderPromoItem::getRecordType, RecordType.REVERSE.getCode()));
        Map<String, ReverseAgg> map = new HashMap<>();
        if (CollUtil.isEmpty(reverses)) {
            return map;
        }
        for (OrderPromoItem r : reverses) {
            String key = buildAggKey(r.getOrderItemId(), r.getOrderPromoId(), r.getPromoSourceType());
            map.computeIfAbsent(key, k -> new ReverseAgg()).add(
                r.getQuantity(),
                r.getDiscountAmount(),
                r.getPlatformShareAmount(),
                r.getMerchantShareAmount());
        }
        return map;
    }

    private static String buildAggKey(Long orderItemId, Long promoId, Long source) {
        return orderItemId + "#" + promoId + "#" + source;
    }

    private OrderPromo newMain(String tenantId, String channel, String parentOrderNo,
                               SourceType source, String promoType, Long activityId,
                               Long userCouponId, String title,
                               BigDecimal discount, BigDecimal platform, BigDecimal merchant) {
        OrderPromo main = new OrderPromo();
        main.setTenantId(tenantId);
        main.setParentOrderNo(parentOrderNo);
        main.setChannelCode(channel);
        main.setPromoSourceType(source.getCode());
        main.setPromoType(promoType);
        main.setActivityId(activityId);
        main.setUserCouponId(Optional.ofNullable(userCouponId).orElse(0L));
        main.setActivityTitle(title);
        main.setDiscountAmount(scale(discount));
        main.setPlatformShareAmount(scale(platform));
        main.setMerchantShareAmount(scale(merchant));
        main.setStatus(PromoStatus.ACTIVE.getCode());
        return main;
    }

    /**
     * 按店促活动 ID 分组（主表挂父单后，同一活动一条主表）。
     */
    private Map<Long, List<SavedOrderItemLine>> collectShopDiscountGroups(List<SavedOrderItemLine> lines) {
        Map<Long, List<SavedOrderItemLine>> groups = new LinkedHashMap<>();
        for (SavedOrderItemLine line : lines) {
            if (line.isGift() || line.getSelectedShopActivityId() == null || isFullGiftType(line.getSelectedPromoType())) {
                continue;
            }
            if (nullToZero(line.getShopDiscountAmount()).compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            groups.computeIfAbsent(line.getSelectedShopActivityId(), k -> new ArrayList<>()).add(line);
        }
        return groups;
    }

    private static boolean isFullGiftType(String promoType) {
        return TYPE_FULL_GIFT.equalsIgnoreCase(promoType);
    }

    private static String normalizeExclusive(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        if (TYPE_SECKILL.equalsIgnoreCase(raw)) {
            return TYPE_SECKILL;
        }
        if (TYPE_SPECIAL.equalsIgnoreCase(raw)) {
            return TYPE_SPECIAL;
        }
        return null;
    }


    /**
     * 写营销子表流水，并把平台承担拆到每一行，同时用「最后一行倒挤」保证加总等于主表。
     * @param hitLines  本组要写的商品行
     * @param orderPromoId  刚插好的主表 id
     * @param platformRate  平台比例
     * @param totalPlatform   平台总承担
     * @param discountGetter   取该行优惠
     */
    private void insertItemsWithLastLineShareAbsorb(String tenantId, String parentOrderNo,
                                                    List<SavedOrderItemLine> hitLines, Long orderPromoId,
                                                    Long sourceCode, BigDecimal platformRate,
                                                    BigDecimal totalPlatform,
                                                    Function<SavedOrderItemLine, BigDecimal> discountGetter) {
        // 前面各行已经分出去的平台金额累计
        BigDecimal allocatedPlatform = BigDecimal.ZERO;
        // 最后一行下标，用来判断「是不是末行」
        int lastIdx = hitLines.size() - 1;
        for (int i = 0; i < hitLines.size(); i++) {
            SavedOrderItemLine line = hitLines.get(i);
            BigDecimal disc = scale(discountGetter.apply(line));
            BigDecimal platform;
            if (i == lastIdx) {
                platform = totalPlatform.subtract(allocatedPlatform);
            } else {
                platform = calcPlatformShare(disc, platformRate);
                allocatedPlatform = allocatedPlatform.add(platform);
            }
            insertItem(tenantId, parentOrderNo, line, orderPromoId, sourceCode, line.getQuantity(), disc, platform, disc.subtract(platform));
        }
    }

    /**
     * 把一组商品行上的优惠金额加总。
     */
    private static BigDecimal sumLineDiscount(List<SavedOrderItemLine> lines, Function<SavedOrderItemLine, BigDecimal> discountGetter) {
        BigDecimal total = BigDecimal.ZERO;
        for (SavedOrderItemLine line : lines) {
            total = total.add(scale(discountGetter.apply(line)));
        }
        return total;
    }

    private static BigDecimal calcPlatformShare(BigDecimal discount, BigDecimal platformRate) {
        return scale(discount).multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale(BigDecimal v) {
        return nullToZero(v).setScale(2, RoundingMode.HALF_UP);
    }
}
