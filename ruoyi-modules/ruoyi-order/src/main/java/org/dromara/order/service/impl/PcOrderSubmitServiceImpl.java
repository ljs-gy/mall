package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.goods.api.domain.RemoteMasterSkuBo;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.RemoteOrderCreateBo;
import org.dromara.order.api.domain.RemoteOrderItemCreateBo;
import org.dromara.order.api.mq.OrderCancelReason;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.OrderTimeoutCancelMessage;
import org.dromara.order.api.mq.SkuRollbackItem;
import org.dromara.order.domain.CartItem;
import org.dromara.order.domain.OrderTimeConfig;
import org.dromara.order.domain.bo.OrderSubmitMysqlTxResult;
import org.dromara.order.domain.bo.RedisReserveResult;
import org.dromara.order.domain.bo.PcBuyNowOrderSubmitBo;
import org.dromara.order.domain.bo.PcCartOrderSubmitBo;
import org.dromara.order.domain.bo.PcCheckoutConfirmBo;
import org.dromara.order.domain.bo.PcCheckoutItemBo;
import org.dromara.order.domain.bo.PcOrderSubmitBaseBo;
import org.dromara.order.domain.bo.SavedOrderItemLine;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;
import org.dromara.order.domain.vo.PcCheckoutGiftItemVo;
import org.dromara.order.domain.vo.PcCheckoutMerchantGroupVo;
import org.dromara.order.domain.vo.PcCheckoutPriceSummaryVo;
import org.dromara.order.domain.vo.PcCheckoutProductVo;
import org.dromara.order.domain.vo.PcOrderSubmitResultVo;
import org.dromara.order.mapper.CartItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.mapper.OrderTimeConfigMapper;
import org.dromara.order.service.IOrderPriceCalculateService;
import org.dromara.order.service.IOrderPromoWriteService;
import org.dromara.order.service.IOrderWriteService;
import org.dromara.order.service.IPcOrderSubmitService;
import org.dromara.order.util.LoginBizCodeHelper;
import org.dromara.order.util.OrderPromoCalculateMapper;
import org.dromara.pm.api.RemotePmService;
import org.dromara.pm.api.domain.RemoteExclusivePromoDeductBo;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PcOrderSubmitServiceImpl implements IPcOrderSubmitService {

    private static final String TYPE_FULL_GIFT = "FULL_GIFT";
    private static final long ORDER_STATUS_UNPAID = 10L;
    private static final long PAY_STATUS_UNPAID = 0L;
    private static final long DEFAULT_UNPAID_CANCEL_MINUTES = 30L;
    /** 提交锁：最多等待获取锁秒数 */
    private static final long SUBMIT_LOCK_WAIT_SECONDS = 3L;
    /** SKU 上架 */
    private static final int SKU_SALE_ON = 1;
    /** SPU 审核通过 */
    private static final int AUDIT_PASSED = 2;
    private static final String MSG_GIFT_INVALID = "满赠赠品已变更或库存不足，请返回重新结算";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final IOrderPriceCalculateService orderPriceCalculateService;
    private final IOrderWriteService orderWriteService;
    private final IOrderPromoWriteService orderPromoWriteService;
    private final OrderMapper orderMapper;
    private final CartItemMapper cartItemMapper;
    private final OrderTimeConfigMapper orderTimeConfigMapper;
    private final RabbitTemplate rabbitTemplate;
    private final LoginBizCodeHelper loginBizCodeHelper;

    @Lazy
    @Autowired
    private PcOrderSubmitServiceImpl self;

    @DubboReference
    private RemoteOrderService remoteOrderService;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    @DubboReference
    private RemotePmService remotePmService;

    public PcOrderSubmitServiceImpl(StringRedisTemplate stringRedisTemplate,
                                    RedissonClient redissonClient,
                                    IOrderPriceCalculateService orderPriceCalculateService,
                                    IOrderWriteService orderWriteService,
                                    IOrderPromoWriteService orderPromoWriteService,
                                    OrderMapper orderMapper,
                                    CartItemMapper cartItemMapper,
                                    OrderTimeConfigMapper orderTimeConfigMapper,
                                    RabbitTemplate rabbitTemplate,
                                    LoginBizCodeHelper loginBizCodeHelper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.orderPriceCalculateService = orderPriceCalculateService;
        this.orderWriteService = orderWriteService;
        this.orderPromoWriteService = orderPromoWriteService;
        this.orderMapper = orderMapper;
        this.cartItemMapper = cartItemMapper;
        this.orderTimeConfigMapper = orderTimeConfigMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.loginBizCodeHelper = loginBizCodeHelper;
    }

    @Override
    public PcOrderSubmitResultVo submitFromCart(PcCartOrderSubmitBo cartBo) {
        return submitOrder(cartBo, true, null);
    }

    @Override
    public PcOrderSubmitResultVo submitBuyNow(PcBuyNowOrderSubmitBo buyNowBo) {
        return submitOrder(buyNowBo, false, buyNowBo.getActivityId());
    }


    private PcOrderSubmitResultVo submitOrder(PcOrderSubmitBaseBo bo, boolean fromCart, Long activityId) {
        if (!LoginHelper.isLogin()) {
            throw new ServiceException("请先登录");
        }
        Long userId = LoginHelper.getUserId();
        String tenantId = TenantHelper.getTenantId();
        String channel = bo.getChannelCode();
        String lockKey = "order:lock:" + tenantId + ":" + channel + ":" + userId;
        // 同一用户同一渠道同时只能有一个提交在跑，防连点/并发双单。
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // leaseTime = -1：启用 Redisson 看门狗自动续期，下单慢也不会锁提前过期被别人抢走；业务结束在 finally 里手动 unlock
            if (!lock.tryLock(SUBMIT_LOCK_WAIT_SECONDS, -1, TimeUnit.SECONDS)) {
                throw new ServiceException("正在处理中，请勿重复点击提交");
            }
            try {
                return doSubmitOrderTransaction(bo, fromCart, activityId, userId, tenantId, channel);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("下单处理中断，请稍后重试");
        }
    }

    private PcOrderSubmitResultVo doSubmitOrderTransaction(PcOrderSubmitBaseBo bo, boolean fromCart, Long activityId,
                                                           Long userId, String tenantId, String channel) {
        if (fromCart) {
            if (activityId != null) {
                throw new ServiceException("秒杀/特价商品不能从购物车下单，请走立即购买");
            }
            validateCartItemsMatchSubmit(bo, userId, tenantId);
        }
        PcCheckoutConfirmVo priceVO = buildOrderPrice(bo, activityId);
        validateCheckoutOrder(priceVO, bo.getExpectedPayAmount(), tenantId);
        String exclusivePromoType = null;
        if (activityId != null) {
            exclusivePromoType = remotePmService.getExclusivePromoType(tenantId, channel, activityId);
            if (StringUtils.isBlank(exclusivePromoType)) {
                throw new ServiceException("促销活动不存在或类型无效，请返回重新结算");
            }
        }
        int shopCount = priceVO.getMerchantGroups().size();
        String parentOrderNo = shopCount <= 1 ? generateOrderNo("S", tenantId) : generateOrderNo("P", tenantId);
        long unpaidCancelMinutes = resolveUnpaidCancelMinutes(tenantId, channel);
        BigDecimal totalPayAmount = scaleMoney(priceVO.getPriceSummary().getFinalPayAmount());
        Long selectedCouponId = priceVO.getCouponSelector() != null ? priceVO.getCouponSelector().getSelectedUserCouponId() : null;
        String finalExclusivePromoType = exclusivePromoType;
        // 记录已经在营销 Redis 里预占成功了哪些资源，方便失败时按原样退回，成功后也能带进超时关单消息。
        RedisReserveResult redisResult = new RedisReserveResult();
        try {
            reserveRedisAssets(activityId, priceVO, userId, tenantId, channel, parentOrderNo, redisResult);
        } catch (Exception e) {
            restoreRedisAssetsSync(redisResult, tenantId, channel, userId, parentOrderNo);
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            log.error("占 Redis 资产异常 parentOrderNo={}", parentOrderNo, e);
            throw new ServiceException("下单失败，请稍后重试");
        }

        OrderSubmitMysqlTxResult mysqlTxResult;
        try {
            mysqlTxResult = self.executeMysqlPhase1(
                bo, activityId, priceVO, userId, tenantId, channel, parentOrderNo, finalExclusivePromoType, selectedCouponId);
        } catch (RuntimeException e) {
            restoreRedisAssetsSync(redisResult, tenantId, channel, userId, parentOrderNo);
            throw e instanceof ServiceException
                ? (ServiceException) e
                : new ServiceException("下单失败，请稍后重试");
        }
        safeRegisterTimeoutCancelMessages(parentOrderNo, userId, tenantId, channel, unpaidCancelMinutes, mysqlTxResult, redisResult);

        // 清空购物车（失败不影响下单成功）
        if (fromCart) {
            List<Long> cartItemIds = collectCartItemIds(bo.getItems());
            if (CollUtil.isNotEmpty(cartItemIds)) {
                try {
                    remoteOrderService.deleteCartItems(tenantId, userId, cartItemIds);
                } catch (Exception e) {
                    log.error("下单成功但清空购物车失败 parentOrderNo={}", parentOrderNo, e);
                }
            }
        }
        PcOrderSubmitResultVo result = new PcOrderSubmitResultVo();
        result.setParentOrderNo(parentOrderNo);
        result.setPayAmount(totalPayAmount);
        result.setAutoCancelMinutes((int) unpaidCancelMinutes);
        return result;
    }

    /**
     * 统一校验地址、结算汇总、金额指纹、满赠赠品。
     */
    private void validateCheckoutOrder(PcCheckoutConfirmVo priceVO, BigDecimal expectedPayAmount, String tenantId) {
        if (priceVO.getAddress() == null || priceVO.getAddress().getAddressId() == null) {
            throw new ServiceException("收货地址不存在，请重新选择");
        }
        if (priceVO.getPriceSummary() == null || CollUtil.isEmpty(priceVO.getMerchantGroups())) {
            throw new ServiceException("结算结果异常，请重新下单");
        }
        validateExpectedPayAmount(expectedPayAmount, priceVO.getPriceSummary().getFinalPayAmount());
        revalidateGiftItems(priceVO, tenantId);
    }

    private PcCheckoutConfirmVo buildOrderPrice(PcOrderSubmitBaseBo bo, Long activityId) {
        PcCheckoutConfirmBo confirmBo = new PcCheckoutConfirmBo();
        confirmBo.setChannelCode(bo.getChannelCode());
        confirmBo.setActivityId(activityId);
        confirmBo.setAddressId(bo.getAddressId());
        confirmBo.setUserCouponId(bo.getUserCouponId());
        Long specifiedCrossId = bo.getSpecifiedCrossActivityId();
        confirmBo.setSpecifiedCrossActivityId(
            specifiedCrossId != null ? specifiedCrossId : -1L);
        confirmBo.setItems(bo.getItems().stream().map(i -> {
            PcCheckoutItemBo item = new PcCheckoutItemBo();
            item.setSpuId(i.getSpuId());
            item.setSkuId(i.getSkuId());
            item.setBuyCount(i.getBuyCount());
            Long lineAct = i.getSpecifiedShopActivityId();
            item.setSpecifiedShopActivityId(lineAct != null ? lineAct : -1L);
            return item;
        }).collect(Collectors.toList()));
        return orderPriceCalculateService.calculate(confirmBo);
    }

    /**
     * 确认每行提交的商品，都对应你自己购物车里真实存在的一行，且没被篡改、数量没超。
     */
    private void validateCartItemsMatchSubmit(PcOrderSubmitBaseBo bo, Long userId, String tenantId) {
        if (CollUtil.isEmpty(bo.getItems())) {
            throw new ServiceException("下单商品清单不能为空");
        }
        List<Long> cartItemIds = collectCartItemIds(bo.getItems());
        if (cartItemIds.size() != bo.getItems().size()) {
            throw new ServiceException("购物车下单每行须指定购物车行 ID");
        }
        if (cartItemIds.size() != new HashSet<>(cartItemIds).size()) {
            throw new ServiceException("下单商品清单存在重复购物车行，请返回重新结算");
        }

        List<CartItem> cartItems = cartItemMapper.selectList(Wrappers.<CartItem>lambdaQuery()
            .eq(CartItem::getTenantId, tenantId)
            .eq(CartItem::getUserinfoId, userId)
            .in(CartItem::getId, cartItemIds));
        if (cartItems.size() != cartItemIds.size()) {
            throw new ServiceException("购物车商品已变更，请返回重新结算");
        }
        Map<Long, CartItem> cartById = cartItems.stream()
            .collect(Collectors.toMap(CartItem::getId, c -> c, (a, b) -> a));

        for (PcCheckoutItemBo item : bo.getItems()) {
            CartItem cart = cartById.get(item.getCartItemId());
            if (cart == null) {
                throw new ServiceException("购物车商品已变更，请返回重新结算");
            }
            if (cart.getSkuId() == null || cart.getQuantity() == null || cart.getQuantity() <= 0) {
                throw new ServiceException("购物车商品数据异常，请返回重新结算");
            }
            if (!cart.getSkuId().equals(item.getSkuId())) {
                throw new ServiceException("购物车商品与提交商品不一致，请返回重新结算");
            }
            if (cart.getSpuId() != null && item.getSpuId() != null && !cart.getSpuId().equals(item.getSpuId())) {
                throw new ServiceException("购物车商品与提交商品不一致，请返回重新结算");
            }
            if (item.getBuyCount() == null || item.getBuyCount() <= 0) {
                throw new ServiceException("购买数量无效，请返回重新结算");
            }
            if (item.getBuyCount() > cart.getQuantity().intValue()) {
                throw new ServiceException("购买数量超过购物车数量，请返回重新结算");
            }
        }
    }

    private static List<Long> collectCartItemIds(List<PcCheckoutItemBo> items) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        for (PcCheckoutItemBo item : items) {
            if (item.getCartItemId() != null && item.getCartItemId() > 0) {
                ids.add(item.getCartItemId());
            }
        }
        return ids;
    }

    /**
     * 校验结算页应付金额与本次重算一致，拦截软变价。
     */
    private void validateExpectedPayAmount(BigDecimal expectedPayAmount, BigDecimal actualFinalPayAmount) {
        BigDecimal expected = scaleMoney(nullToZero(expectedPayAmount));
        BigDecimal actual = scaleMoney(nullToZero(actualFinalPayAmount));
        if (expected.compareTo(actual) != 0) {
            throw new ServiceException("订单金额已变更，请返回重新结算");
        }
    }

    /**
     * 落库前复验满赠赠品：存在、可售、物理库存充足。
     */
    private void revalidateGiftItems(PcCheckoutConfirmVo priceVO, String tenantId) {
        if (priceVO == null || CollUtil.isEmpty(priceVO.getMerchantGroups())) {
            return;
        }

        Map<Long, Integer> needBySku = collectGiftNeeds(priceVO.getMerchantGroups());
        if (needBySku.isEmpty()) {
            return;
        }
        List<String> skuIdStrs = needBySku.keySet().stream().map(String::valueOf).collect(Collectors.toList());
        List<RemoteMasterSkuBo> goodsList;
        try {
            goodsList = remoteGoodsService.selectBySkuIds(tenantId, skuIdStrs);
        } catch (Exception e) {
            log.error("提交订单复验满赠赠品失败 tenantId={} skuIds={}", tenantId, skuIdStrs, e);
            throw new ServiceException(MSG_GIFT_INVALID);
        }
        Map<Long, RemoteMasterSkuBo> goodsMap = CollUtil.isEmpty(goodsList) ? Collections.emptyMap() :
            goodsList.stream()
                .filter(g -> g != null && g.getSkuId() != null)
                .collect(Collectors.toMap(RemoteMasterSkuBo::getSkuId, g -> g, (a, b) -> a));
        for (Map.Entry<Long, Integer> entry : needBySku.entrySet()) {
            Long giftSkuId = entry.getKey();
            int needQty = entry.getValue();
            RemoteMasterSkuBo goods = goodsMap.get(giftSkuId);
            String title = (goods != null && StringUtils.isNotBlank(goods.getTitle())) ? goods.getTitle() : String.valueOf(giftSkuId);
            if (!isGiftSkuSaleable(goods)) {
                log.info("提交复验：满赠赠品不可售 giftSkuId={} title={}", giftSkuId, title);
                throw new ServiceException(MSG_GIFT_INVALID);
            }
            int stock = goods.getOriginStock() != null ? goods.getOriginStock() : 0;
            if (stock < needQty) {
                log.info("提交复验：满赠赠品库存不足 giftSkuId={} need={} stock={}", giftSkuId, needQty, stock);
                throw new ServiceException(MSG_GIFT_INVALID);
            }
        }
    }

    /**
     * 遍历店铺商品行，按满赠活动去重后汇总各赠品 SKU 数量。
     */
    private Map<Long, Integer> collectGiftNeeds(List<PcCheckoutMerchantGroupVo> groups) {
        // 记录整单最终要扣的赠品数量
        Map<Long, Integer> needBySku = new HashMap<>();
        // 已处理过的满赠活动 ID
        Set<Long> processedActIds = new HashSet<>();
        for (PcCheckoutMerchantGroupVo group : groups) {
            if (CollUtil.isEmpty(group.getProductList())) {
                continue;
            }
            for (PcCheckoutProductVo item : group.getProductList()) {
                Long actId = item.getSelectedShopActivityId();
                if (actId == null || !TYPE_FULL_GIFT.equalsIgnoreCase(item.getSelectedPromoType())
                    || CollUtil.isEmpty(item.getGiftList())) {
                    continue;
                }
                // 同一满赠活动多行挂载时，只累加一次赠品
                if (!processedActIds.add(actId)) {
                    continue;
                }
                for (PcCheckoutGiftItemVo gift : item.getGiftList()) {
                    if (gift.getGiftSkuId() == null) {
                        throw new ServiceException(MSG_GIFT_INVALID);
                    }
                    int qty = (gift.getGiftCount() == null || gift.getGiftCount() <= 0) ? 1 : gift.getGiftCount();
                    needBySku.merge(gift.getGiftSkuId(), qty, Integer::sum);
                }
            }
        }
        return needBySku;
    }

    private boolean isGiftSkuSaleable(RemoteMasterSkuBo goods) {
        if (goods == null) {
            return false;
        }
        return Integer.valueOf(SKU_SALE_ON).equals(goods.getSkuSaleStatus())
            && Integer.valueOf(AUDIT_PASSED).equals(goods.getAuditStatus());
    }




    @GlobalTransactional(rollbackFor = Exception.class, name = "order-submit-mysql-phase1")
    public OrderSubmitMysqlTxResult executeMysqlPhase1(PcOrderSubmitBaseBo bo,
                                                       Long activityId,
                                                       PcCheckoutConfirmVo priceVO,
                                                       Long userId,
                                                       String tenantId,
                                                       String channel,
                                                       String parentOrderNo,
                                                       String exclusivePromoType,
                                                       Long selectedCouponId) {
        List<OrderSubmitMysqlTxResult.SubOrderTimeoutPayload> subOrders =
            persistOrdersOnly(bo, activityId, priceVO, userId, tenantId, channel, parentOrderNo, exclusivePromoType);
        if (CollUtil.isEmpty(subOrders)) {
            throw new ServiceException("创建订单失败，请稍后重试");
        }
        List<SkuRollbackItem> allRollbackItems = new ArrayList<>();
        for (OrderSubmitMysqlTxResult.SubOrderTimeoutPayload sub : subOrders) {
            if (CollUtil.isNotEmpty(sub.getRollbackItems())) {
                allRollbackItems.addAll(sub.getRollbackItems());
            }
        }
        if (CollUtil.isEmpty(allRollbackItems)) {
            throw new ServiceException("创建订单失败，请稍后重试");
        }
        // 专享配额预占只走 Redis（阶段外已 deduct），DB 支付成功再记 sold_stock
        deductPhysicalStock(allRollbackItems, priceVO);
        Long usedCouponId = useCouponIfPresent(tenantId, userId, selectedCouponId);
        log.info("提单阶段一 MySQL 全局事务完成 parentOrderNo={} subOrders={} skuLines={} couponId={}",
            parentOrderNo, subOrders.size(), allRollbackItems.size(), usedCouponId);
        return new OrderSubmitMysqlTxResult(subOrders, usedCouponId);
    }

    /**
     * 扣减物理库存：同 SKU 聚合一次 RPC，缺货时带商品名提示。
     */
    private void deductPhysicalStock(List<SkuRollbackItem> rollbackItems, PcCheckoutConfirmVo priceVO) {
        if (CollUtil.isEmpty(rollbackItems)) {
            return;
        }
        Map<Long, String> skuNameMap = extractSkuNameMap(priceVO);
        Map<Long, Integer> skuTotalQtyMap = new HashMap<>();
        for (SkuRollbackItem item : rollbackItems) {
            if (item == null || item.getSkuId() == null
                || item.getBuyCount() == null || item.getBuyCount() <= 0) {
                continue;
            }
            skuTotalQtyMap.merge(item.getSkuId(), item.getBuyCount(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry : skuTotalQtyMap.entrySet()) {
            Long skuId = entry.getKey();
            Integer totalQty = entry.getValue();
            if (!remoteGoodsService.deductPhysicalStock(skuId, totalQty)) {
                String goodsName = skuNameMap.getOrDefault(skuId, "商品(SKU:" + skuId + ")");
                log.warn("物理库存扣减不足 skuId={} goodsName={} needQty={}", skuId, goodsName, totalQty);
                throw new ServiceException("很抱歉，【" + goodsName + "】库存不足，请返回重新结算");
            }
        }
    }

    /**
     * 从结算视图解析 SKU 展示名（正品标题+规格 / 赠品），零额外 RPC。
     */
    private Map<Long, String> extractSkuNameMap(PcCheckoutConfirmVo priceVO) {
        Map<Long, String> nameMap = new HashMap<>();
        if (priceVO == null || CollUtil.isEmpty(priceVO.getMerchantGroups())) {
            return nameMap;
        }
        for (PcCheckoutMerchantGroupVo group : priceVO.getMerchantGroups()) {
            if (CollUtil.isEmpty(group.getProductList())) {
                continue;
            }
            for (PcCheckoutProductVo product : group.getProductList()) {
                if (product.getSkuId() != null) {
                    String spec = StringUtils.isNotBlank(product.getSpecText()) ? " " + product.getSpecText() : "";
                    nameMap.putIfAbsent(product.getSkuId(),
                        StringUtils.blankToDefault(product.getTitle(), "商品") + spec);
                }
                if (CollUtil.isEmpty(product.getGiftList())) {
                    continue;
                }
                for (PcCheckoutGiftItemVo gift : product.getGiftList()) {
                    if (gift.getGiftSkuId() == null) {
                        continue;
                    }
                    String giftTitle = StringUtils.isNotBlank(gift.getTitle()) ? gift.getTitle() : "赠品";
                    String giftSpec = StringUtils.isNotBlank(gift.getSpecText()) ? " " + gift.getSpecText() : "";
                    nameMap.putIfAbsent(gift.getGiftSkuId(), "【赠品】" + giftTitle + giftSpec);
                }
            }
        }
        return nameMap;
    }


    private Long useCouponIfPresent(String tenantId, Long userId, Long selectedCouponId) {
        if (selectedCouponId == null) {
            return null;
        }
        int rows = remotePmService.useCoupon(tenantId, selectedCouponId, userId);
        if (rows <= 0) {
            throw new ServiceException("优惠券已被占用或已失效，请返回重新结算");
        }
        return selectedCouponId;
    }


    private List<OrderSubmitMysqlTxResult.SubOrderTimeoutPayload> persistOrdersOnly(
        PcOrderSubmitBaseBo bo, Long activityId, PcCheckoutConfirmVo priceVO,
        Long userId, String tenantId, String channel,
        String parentOrderNo, String exclusivePromoType) {
        String receiverAddressText = String.format("%s %s %s %s",
            StringUtils.blankToDefault(priceVO.getAddress().getProvinceName(), ""),
            StringUtils.blankToDefault(priceVO.getAddress().getCityName(), ""),
            StringUtils.blankToDefault(priceVO.getAddress().getDistrictName(), ""),
            StringUtils.blankToDefault(priceVO.getAddress().getDetailAddress(), "")).trim();

        List<OrderSubmitMysqlTxResult.SubOrderTimeoutPayload> subOrders = new ArrayList<>();
        List<SavedOrderItemLine> promoLines = new ArrayList<>();
        String memberBcode = loginBizCodeHelper.requireUserinfoCode();
        Long userLevelId = LoginHelper.getDistLevelId();
        boolean singleShop = priceVO.getMerchantGroups().size() <= 1;

        for (PcCheckoutMerchantGroupVo group : priceVO.getMerchantGroups()) {
            String subOrderNo = singleShop ? parentOrderNo : generateOrderNo("S", tenantId);
            BigDecimal shopCouponDiscount = nullToZero(group.getShopCouponDiscount());
            BigDecimal shopDiscountSum = nullToZero(group.getShopPromoDiscount())
                .add(nullToZero(group.getShopCrossReduceDiscount()))
                .add(shopCouponDiscount);
            BigDecimal shopFreightAmount = scaleMoney(nullToZero(group.getShopFreightAmount()));
            BigDecimal shopFinalPay = scaleMoney(nullToZero(group.getShopPayAmount()).add(shopFreightAmount));
            RemoteOrderCreateBo subOrder = new RemoteOrderCreateBo();
            subOrder.setTenantId(tenantId);
            subOrder.setOrderNo(subOrderNo);
            subOrder.setParentOrderNo(parentOrderNo);
            subOrder.setChannelCode(channel);
            subOrder.setMemberCode(group.getMemberCode());
            subOrder.setUserinfoId(userId);
            subOrder.setMemberBcode(memberBcode);
            subOrder.setUserLevelId(userLevelId);
            subOrder.setOrderStatus(ORDER_STATUS_UNPAID);
            subOrder.setPayStatus(PAY_STATUS_UNPAID);
            subOrder.setGoodsTotalAmount(scaleMoney(group.getShopTotalAmount()));
            subOrder.setFreightAmount(shopFreightAmount);
            subOrder.setDiscountAmount(scaleMoney(shopDiscountSum));
            subOrder.setPayAmount(shopFinalPay);
            subOrder.setConsigneeName(priceVO.getAddress().getConsigneeName());
            subOrder.setConsigneeMobile(priceVO.getAddress().getConsigneeMobile());
            subOrder.setReceiverAddressText(receiverAddressText);
            subOrder.setBuyerRemark(bo.getBuyerRemark());
            if (orderWriteService.createOrder(subOrder) == null) {
                throw new ServiceException("创建订单失败，请稍后重试");
            }
            List<SkuRollbackItem> subItems = new ArrayList<>();
            saveOrderItemsOnly(group, subOrderNo, activityId, exclusivePromoType, tenantId,
                subItems, promoLines);
            subOrders.add(new OrderSubmitMysqlTxResult.SubOrderTimeoutPayload(
                subOrderNo,
                subItems,
                activityId,
                exclusivePromoType));
        }
        orderPromoWriteService.writeOnSubmit(tenantId, channel, parentOrderNo, activityId, exclusivePromoType, priceVO, promoLines);
        return subOrders;
    }



    /**
     * 营销 Redis 预占
     */
    private void reserveRedisAssets(Long activityId, PcCheckoutConfirmVo priceVO,
                                    Long userId, String tenantId, String channel,
                                    String parentOrderNo,
                                    RedisReserveResult result) {
        // 处理秒杀/特价专享单
        if (activityId != null) {
            RemoteExclusivePromoDeductBo deductBo = OrderPromoCalculateMapper.toExclusiveDeductBo(
                tenantId, channel, activityId, userId, parentOrderNo, priceVO);
            remotePmService.deductExclusivePromoStock(deductBo);
            result.setExclusiveDeductBo(deductBo);
        }

        // 跨店预算
        PcCheckoutPriceSummaryVo summary = priceVO.getPriceSummary();
        BigDecimal crossDiscount = summary != null ? nullToZero(summary.getTotalCrossReduceDiscount()) : BigDecimal.ZERO;
        Long crossActId = summary != null ? summary.getSelectedCrossActivityId() : null;
        if (crossDiscount.compareTo(BigDecimal.ZERO) > 0 && crossActId != null) {
            Long deductedId = deductCrossPromoBudgetPool(tenantId, channel, crossActId, crossDiscount, parentOrderNo);
            if (deductedId == null) {
                throw new ServiceException("抱歉，平台跨店大促补贴预算已耗尽，请返回重新结算");
            }
            result.setCrossActivityId(deductedId);
            result.setCrossDiscountAmount(crossDiscount);
        }

        // 活动参加次数
        if (activityId == null) {
            List<Long> joinActivityIds = collectPromoJoinActivityIds(priceVO, crossActId, crossDiscount);
            if (CollUtil.isNotEmpty(joinActivityIds)) {
                List<Long> actualReservedIds = remotePmService.reservePromoJoinQuota(
                    tenantId, channel, userId, joinActivityIds, parentOrderNo);
                if (CollUtil.isNotEmpty(actualReservedIds)) {
                    result.setPromoJoinActivityIds(actualReservedIds);
                }
            }
        }
    }

    /**
     * 下单失败且订单还没落成时，按本请求已占记录，把营销 Redis 同步退掉
     */
    private void restoreRedisAssetsSync(RedisReserveResult result, String tenantId, String channel, Long userId, String parentOrderNo) {
        if (result == null) {
            return;
        }
        // 专享 → 加回活动库存/限购
        if (result.getExclusiveDeductBo() != null) {
            remotePmService.restoreExclusivePromoStock(result.getExclusiveDeductBo());
        }
        // 跨店预算 → 把扣掉的预算加回
        if (result.getCrossActivityId() != null && result.getCrossDiscountAmount() != null) {
            remotePmService.restoreCrossPromoBudget(
                tenantId, channel, result.getCrossActivityId(), result.getCrossDiscountAmount());
        }
        // 参与次数 → 每个活动对该用户 -1
        if (CollUtil.isNotEmpty(result.getPromoJoinActivityIds())) {
            remotePmService.rollbackPromoJoinQuota(
                tenantId, channel, userId, result.getPromoJoinActivityIds(), parentOrderNo);
        }
    }

    /**
     * 汇总本单需要占「活动参与次数」的活动 ID 列表
     */
    private List<Long> collectPromoJoinActivityIds(PcCheckoutConfirmVo checkoutVO,
                                                   Long selectedCrossActivityId,
                                                   BigDecimal totalCrossDiscount) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (checkoutVO != null && CollUtil.isNotEmpty(checkoutVO.getMerchantGroups())) {
            for (PcCheckoutMerchantGroupVo group : checkoutVO.getMerchantGroups()) {
                if (CollUtil.isEmpty(group.getProductList())) {
                    continue;
                }
                for (PcCheckoutProductVo item : group.getProductList()) {
                    Long actId = item.getSelectedShopActivityId();
                    if (actId != null && actId > 0) {
                        ids.add(actId);
                    }
                }
            }
        }
        if (selectedCrossActivityId != null && selectedCrossActivityId > 0
            && totalCrossDiscount != null && totalCrossDiscount.compareTo(BigDecimal.ZERO) > 0) {
            ids.add(selectedCrossActivityId);
        }
        return new ArrayList<>(ids);
    }

    /**
     * 按子单各投递一条延时关单消息。
     * 库存、支付、取消都是按子单（店铺）独立的，延时关单也必须跟到子单粒度，否则会误关、误回滚。
     * 场景：购物车一次下单买两家店，中间发生了「只付一家」，另一家每付，只关其中一单
     */
    private void safeRegisterTimeoutCancelMessages(String parentOrderNo, Long userId,
                                                   String tenantId, String channel,
                                                   long unpaidCancelMinutes,
                                                   OrderSubmitMysqlTxResult mysqlTxResult,
                                                   RedisReserveResult redis) {
        if (mysqlTxResult == null || CollUtil.isEmpty(mysqlTxResult.getSubOrders())) {
            return;
        }
        // 把未支付自动取消的分钟数换成毫秒，给延时消息用。
        long ttlMillis = unpaidCancelMinutes * 60 * 1000L;
        Long usedCouponId = mysqlTxResult.getUsedUserCouponId();
        for (OrderSubmitMysqlTxResult.SubOrderTimeoutPayload sub : mysqlTxResult.getSubOrders()) {
            if (sub == null || StringUtils.isBlank(sub.getOrderNo())) {
                continue;
            }
            try {
                OrderTimeoutCancelMessage cancelMsg = OrderTimeoutCancelMessage.builder()
                    .tenantId(tenantId)
                    .channelCode(channel)
                    .userId(userId)
                    .orderNo(sub.getOrderNo())
                    .parentOrderNo(parentOrderNo)
                    .cancelReason(OrderCancelReason.UNPAID_TIMEOUT)
                    .userCouponId(usedCouponId)
                    .activityId(sub.getActivityId())
                    .exclusivePromoType(sub.getExclusivePromoType())
                    .crossActivityId(redis != null ? redis.getCrossActivityId() : null)
                    .crossDiscountAmount(redis != null ? redis.getCrossDiscountAmount() : null)
                    .promoJoinActivityIds(redis != null ? redis.getPromoJoinActivityIds() : null)
                    .rollbackItems(sub.getRollbackItems())
                    .build();

                rabbitTemplate.convertAndSend(
                    OrderMqConstants.EXCHANGE_ORDER_DELAY,
                    OrderMqConstants.KEY_ORDER_TIMEOUT_DELAY,
                    cancelMsg,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlMillis);
                        return message;
                    });
                log.info("已投递子单延时关单消息 orderNo={} parentOrderNo={}", sub.getOrderNo(), parentOrderNo);
            } catch (Exception e) {
                log.error("子单延时关单消息投递异常，保留订单继续放行 tenantId={} orderNo={} parentOrderNo={}",
                    tenantId, sub.getOrderNo(), parentOrderNo, e);
            }
        }
    }

    /**
     * 写本店订单明细（不扣库存），收集回滚 SKU 与营销双写行。
     */
    private void saveOrderItemsOnly(
        PcCheckoutMerchantGroupVo group, String subOrderNo, Long activityId, String exclusivePromoType,
        String tenantId, List<SkuRollbackItem> rollbackItems, List<SavedOrderItemLine> promoLines) {

        List<PcCheckoutProductVo> productList = group.getProductList();
        // 同店同一满赠活动可能挂在多行上，赠品只写一次
        Set<Long> giftActivityWritten = new HashSet<>();

        for (PcCheckoutProductVo item : productList) {
            BigDecimal itemSubTotal = nullToZero(item.getSubTotalAmount());
            BigDecimal itemShopDiscount = nullToZero(item.getShopDiscountAmount());
            BigDecimal itemCrossDiscount = nullToZero(item.getCrossDiscountAmount());
            BigDecimal itemCouponDiscount = nullToZero(item.getCouponDiscountAmount());
            BigDecimal itemRealPay = nullToZero(item.getRealPayAmount());
            RemoteOrderItemCreateBo orderItem = new RemoteOrderItemCreateBo();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderNo(subOrderNo);
            orderItem.setSpuId(item.getSpuId());
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSkuCode(StringUtils.blankToDefault(item.getSkuCode(), ""));
            orderItem.setMemberCode(group.getMemberCode());
            orderItem.setGoodsTitle(item.getTitle());
            orderItem.setSpecText(item.getSpecText());
            orderItem.setCoverImage(item.getMainPicture());
            orderItem.setPriceSales(scaleMoney(item.getActualUnitPrice()));
            orderItem.setQuantity(item.getBuyCount() == null ? 0L : item.getBuyCount().longValue());
            orderItem.setIsGift(0L);
            orderItem.setSubTotal(scaleMoney(itemSubTotal));
            orderItem.setRealPayAmount(scaleMoney(itemRealPay));
            // 行均摊单价 = 实付 / 数量（向下截断到分）
            orderItem.setSharePrice(calcSharePrice(orderItem.getRealPayAmount(), orderItem.getQuantity()));
            Long itemId = orderWriteService.createOrderItem(orderItem);
            if (itemId == null) {
                throw new ServiceException("创建订单明细失败");
            }
            rollbackItems.add(SkuRollbackItem.builder()
                .skuId(item.getSkuId())
                .buyCount(item.getBuyCount())
                .build());
            promoLines.add(SavedOrderItemLine.builder()
                .orderItemId(itemId)
                .orderNo(subOrderNo)
                .memberCode(group.getMemberCode())
                .skuId(item.getSkuId())
                .skuCode(item.getSkuCode())
                .quantity(item.getBuyCount() == null ? 0 : item.getBuyCount())
                .gift(false)
                .promoActivityId(activityId)
                .promoType(activityId != null ? exclusivePromoType : null)
                .shopDiscountAmount(scaleMoney(itemShopDiscount))
                .crossDiscountAmount(scaleMoney(itemCrossDiscount))
                .couponDiscountAmount(scaleMoney(itemCouponDiscount))
                .selectedShopActivityId(item.getSelectedShopActivityId())
                .selectedPromoType(item.getSelectedPromoType())
                .selectedActivityTitle(item.getSelectedActivityTitle())
                .selectedShopPlatformShareRate(item.getSelectedShopPlatformShareRate())
                .selectedShopMerchantShareRate(item.getSelectedShopMerchantShareRate())
                .build());
        }

        if (CollUtil.isNotEmpty(productList)) {
            for (PcCheckoutProductVo item : productList) {
                Long actId = item.getSelectedShopActivityId();
                if (actId == null
                    || !TYPE_FULL_GIFT.equalsIgnoreCase(item.getSelectedPromoType())
                    || CollUtil.isEmpty(item.getGiftList())
                    || giftActivityWritten.contains(actId)) {
                    continue;
                }
                giftActivityWritten.add(actId);

                for (PcCheckoutGiftItemVo gift : item.getGiftList()) {
                    if (gift.getGiftSkuId() == null) {
                        continue;
                    }
                    int giftQty = gift.getGiftCount() == null ? 1 : gift.getGiftCount();
                    String giftTitle = StringUtils.isNotBlank(gift.getTitle()) ? gift.getTitle() : "赠品";
                    RemoteOrderItemCreateBo giftItem = new RemoteOrderItemCreateBo();
                    giftItem.setTenantId(tenantId);
                    giftItem.setOrderNo(subOrderNo);
                    giftItem.setSpuId(gift.getSpuId() != null ? gift.getSpuId() : 0L);
                    giftItem.setSkuId(gift.getGiftSkuId());
                    giftItem.setSkuCode(StringUtils.blankToDefault(gift.getSkuCode(), ""));
                    giftItem.setMemberCode(group.getMemberCode());
                    giftItem.setGoodsTitle("【赠品】" + giftTitle);
                    giftItem.setSpecText(StringUtils.blankToDefault(gift.getSpecText(), "赠品规格"));
                    giftItem.setPriceSales(BigDecimal.ZERO);
                    giftItem.setQuantity((long) giftQty);
                    giftItem.setIsGift(1L);
                    giftItem.setTierRuleId(gift.getTierRuleId());
                    giftItem.setSubTotal(BigDecimal.ZERO);
                    giftItem.setRealPayAmount(BigDecimal.ZERO);
                    giftItem.setSharePrice(calcSharePrice(BigDecimal.ZERO, giftItem.getQuantity()));
                    Long giftItemId = orderWriteService.createOrderItem(giftItem);
                    if (giftItemId == null) {
                        throw new ServiceException("创建赠品订单明细失败");
                    }
                    rollbackItems.add(SkuRollbackItem.builder()
                        .skuId(gift.getGiftSkuId())
                        .buyCount(giftQty)
                        .build());
                    promoLines.add(SavedOrderItemLine.builder()
                        .orderItemId(giftItemId)
                        .orderNo(subOrderNo)
                        .memberCode(group.getMemberCode())
                        .skuId(gift.getGiftSkuId())
                        .skuCode(gift.getSkuCode())
                        .quantity(giftQty)
                        .gift(true)
                        .promoActivityId(actId)
                        .promoType(TYPE_FULL_GIFT)
                        .shopDiscountAmount(BigDecimal.ZERO)
                        .crossDiscountAmount(BigDecimal.ZERO)
                        .couponDiscountAmount(BigDecimal.ZERO)
                        .selectedShopActivityId(actId)
                        .selectedPromoType(TYPE_FULL_GIFT)
                        .selectedActivityTitle(item.getSelectedActivityTitle())
                        .build());
                }
            }
        }
    }

    /**
     * 按结算命中的跨店活动 ID 扣减平台补贴预算池
     */
    private Long deductCrossPromoBudgetPool(String tenant, String channel, Long crossActivityId,
                                            BigDecimal totalCrossDiscount, String parentOrderNo) {
        if (crossActivityId == null || totalCrossDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (remotePmService.deductCrossPromoBudget(tenant, channel, crossActivityId, totalCrossDiscount, parentOrderNo)) {
            return crossActivityId;
        }
        return null;
    }

    /**
     * 生成业务订单号，尽量保证同一秒内并发也不容易撞号。
     * {前缀}{时间到秒}{租户哈希2位}{序列6位}
     * 例：S/P + 20260905175300 + 37 + 000123
     * 单店用 S 且父子同号；多店父单用 P、子单用 S。
     */
    private String generateOrderNo(String prefix, String tenantId) {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String seqKey = "order:seq:" + timeStr;
        Long seq = stringRedisTemplate.opsForValue().increment(seqKey);
        if (seq != null && seq == 1L) {
            stringRedisTemplate.expire(seqKey, Duration.ofSeconds(5));
        }
        int tenantHash = Math.abs(tenantId.hashCode()) % 100;
        long finalSeq = (seq != null ? seq : ThreadLocalRandom.current().nextInt(1000, 9999)) % 1000000;
        return String.format("%s%s%02d%06d", prefix, timeStr, tenantHash, finalSeq);
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scaleMoney(BigDecimal yuan) {
        return nullToZero(yuan).setScale(2, RoundingMode.HALF_UP);
    }


    /**
     * 行均摊单价：实付 ÷ 件数，向下截断到分，保证 sharePrice × quantity ≤ realPayAmount。
     */
    private static BigDecimal calcSharePrice(BigDecimal realPayAmount, Long quantity) {
        if (quantity == null || quantity <= 0L) {
            return scaleMoney(BigDecimal.ZERO);
        }
        return scaleMoney(realPayAmount).divide(BigDecimal.valueOf(quantity), 2, RoundingMode.DOWN);
    }


    /**
     * 查询订单交易时效配置表，返回未支付订单多久自动关单的分钟数，给后面延时 MQ 用。
     */
    private long resolveUnpaidCancelMinutes(String tenantId, String channel) {
        OrderTimeConfig config = orderTimeConfigMapper.selectOne(
            Wrappers.<OrderTimeConfig>lambdaQuery()
                .eq(OrderTimeConfig::getTenantId, tenantId)
                .eq(OrderTimeConfig::getChannelCode, channel)
                .eq(OrderTimeConfig::getStatus, 1L)
                .last("LIMIT 1"));
        if (config != null && config.getUnpaidCancelMinutes() != null && config.getUnpaidCancelMinutes() > 0) {
            return config.getUnpaidCancelMinutes();
        }
        return DEFAULT_UNPAID_CANCEL_MINUTES;
    }


}
