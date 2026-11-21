package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.goods.api.domain.RemoteMasterSkuBo;
import org.dromara.pm.api.domain.RemoteAvailableCouponBo;
import org.dromara.pm.api.domain.RemoteCouponSelectorBo;
import org.dromara.pm.api.domain.RemoteDisabledCouponBo;
import org.dromara.pm.api.domain.RemotePromoCalculateBo;
import org.dromara.pm.api.domain.RemotePromoCalculateResult;
import org.dromara.pm.api.domain.RemotePromoGiftItemBo;
import org.dromara.pm.api.domain.RemotePromoLineItemBo;
import org.dromara.pm.api.domain.RemotePromoOptionBo;
import org.dromara.pm.api.domain.RemotePromoShopGroupBo;
import org.dromara.pm.api.domain.PromoCalculateModes;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.domain.PromoTierGift;
import org.dromara.pm.domain.PromoTierRule;
import org.dromara.pm.domain.bo.CouponCandidateInternal;
import org.dromara.pm.domain.bo.CouponItemScopeContext;
import org.dromara.pm.domain.bo.CouponMatchResult;
import org.dromara.pm.domain.bo.CrossPromoCalculationResult;
import org.dromara.pm.domain.bo.EligibleItemMetrics;
import org.dromara.pm.domain.bo.PromoItemScopeContext;
import org.dromara.pm.domain.bo.PromoLineContext;
import org.dromara.pm.domain.enums.UserScopeBiz;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.service.ICheckoutPromoCalculateService;
import org.dromara.pm.service.PromoUserQuotaService;
import org.dromara.pm.utils.MoneyFenUtils;
import org.dromara.pm.utils.PromoObjectAllocator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 结算页促销算价（店促装包与购物车预览共用；通过 {@link PromoCalculateModes} 区分出参）
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CheckoutPromoCalculateServiceImpl implements ICheckoutPromoCalculateService {

    private static final String TYPE_SECKILL = "SECKILL";
    private static final String TYPE_SPECIAL = "SPECIAL_PRICE";
    private static final String TYPE_FULL_REDUCE = "FULL_REDUCE";
    private static final String TYPE_FULL_DISCOUNT = "FULL_DISCOUNT";
    private static final String TYPE_FULL_GIFT = "FULL_GIFT";
    private static final String TYPE_CROSS_FULL_REDUCE = "CROSS_FULL_REDUCE";
    private static final String TYPE_CROSS_FULL_DISCOUNT = "CROSS_FULL_DISCOUNT";
    /** SKU 上架 */
    private static final int SKU_SALE_ON = 1;
    /** SPU 审核通过 */
    private static final int AUDIT_PASSED = 2;
    private static final String COUPON_TYPE_REDUCE = "REDUCE";

    /**
     * 合资格统计：按行原价小计
     */
    private static final Function<RemotePromoLineItemBo, BigDecimal> AMOUNT_SUBTOTAL = line ->
        line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO;

    /**
     * 跨店用
     */
    private static final Function<RemotePromoLineItemBo, BigDecimal> AMOUNT_REMAIN_AFTER_SHOP = line -> {
        BigDecimal sub = line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO;
        BigDecimal shop = line.getShopDiscountAmount() != null ? line.getShopDiscountAmount() : BigDecimal.ZERO;
        return sub.subtract(shop).max(BigDecimal.ZERO);
    };

    private final StringRedisTemplate stringRedisTemplate;
    private final CouponUserRelMapper couponUserRelMapper;
    private final PromoUserQuotaService promoUserQuotaService;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    @Override
    public RemotePromoCalculateResult calculate(RemotePromoCalculateBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getTenantId()) || StringUtils.isBlank(bo.getChannelCode())) {
            throw new ServiceException("促销算价参数不完整");
        }
        if (CollUtil.isEmpty(bo.getShopGroups())) {
            throw new ServiceException("结算商品不能为空");
        }

        String tenant = bo.getTenantId();
        String channel = bo.getChannelCode();
        Long userId = bo.getUserId();
        Long userLevelId = bo.getUserLevelId();
        Date now = new Date();
        List<RemotePromoShopGroupBo> groups = bo.getShopGroups();

        // 秒杀/特价专享通道：活动价改价后直接返回，不叠店促/跨店/券
        if (bo.getExclusiveActivityId() != null) {
            applyExclusivePromoPrice(groups, bo.getExclusiveActivityId(), tenant, channel, userId, userLevelId, now);
            return buildExclusiveResult(groups);
        }

        // 汇总各店商品小计，并累计全单商品总金额
        BigDecimal totalProductAmount = BigDecimal.ZERO;
        for (RemotePromoShopGroupBo group : groups) {
            // 本店所有商品行小计之和
            BigDecimal shopTotal = group.getLines().stream()
                .map(line -> line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            group.setShopTotalAmount(shopTotal);
            totalProductAmount = totalProductAmount.add(shopTotal);
        }

        RemotePromoCalculateResult result = new RemotePromoCalculateResult();

        // 渠道是否存在进行中促销
        Boolean hasActivePromos = stringRedisTemplate.hasKey(PmCacheKeys.promoActiveIds(tenant, channel));

        BigDecimal totalShopPromoDiscount = BigDecimal.ZERO;
        BigDecimal totalCrossReduceDiscount = BigDecimal.ZERO;
        CrossPromoCalculationResult crossResult = null;

        boolean cartPreview = PromoCalculateModes.isCartPreview(bo.getCalculateMode());

        if (hasActivePromos) {
            for (RemotePromoShopGroupBo group : groups) {
                // 店内活动计算
                BigDecimal shopDiscount = calculateShopPromoByPackages(group, tenant, channel, userId, userLevelId, now, cartPreview);
                group.setShopPromoDiscount(shopDiscount);
                group.setShopPayAmount(group.getShopTotalAmount().subtract(shopDiscount).max(BigDecimal.ZERO));
                totalShopPromoDiscount = totalShopPromoDiscount.add(shopDiscount);
            }
            // 跨店活动计算
            crossResult = calculateCrossPromoMutex(groups, totalProductAmount, tenant, channel, userId, userLevelId, now, bo.getSpecifiedCrossActivityId());
            totalCrossReduceDiscount = crossResult.getDiscountAmount();
            for (RemotePromoShopGroupBo group : groups) {
                BigDecimal shopRealPay = group.getShopTotalAmount()
                    .subtract(group.getShopPromoDiscount())
                    .subtract(group.getShopCrossReduceDiscount())
                    .max(BigDecimal.ZERO);
                group.setShopPayAmount(shopRealPay);
            }
        } else {
            assertNoSpecifiedPromosWhenInactive(groups, bo.getSpecifiedCrossActivityId());
            resetGroupsToNoPromoState(groups);
        }

        List<PromoLineContext> lineContexts = flattenLineContexts(groups);

        markCrossEligibility(lineContexts, crossResult, tenant, channel);

        BigDecimal actualCrossDiscount = simulateAndApplyCrossDiscount(lineContexts, totalCrossReduceDiscount);
        if (actualCrossDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            clearCrossSelection(crossResult);
            actualCrossDiscount = BigDecimal.ZERO;
            lineContexts.forEach(PromoLineContext::resetCrossDiscount);
        } else if (crossResult != null) {
            crossResult.setDiscountAmount(actualCrossDiscount);
        }
        totalCrossReduceDiscount = actualCrossDiscount;

        BigDecimal baseAmountForCoupon = totalProductAmount
            .subtract(totalShopPromoDiscount)
            .subtract(totalCrossReduceDiscount);

        CouponMatchResult couponMatch = matchAndSelectBestCoupon(userId, lineContexts, baseAmountForCoupon, bo.getUserCouponId(), tenant, channel, now);
        markCouponEligibility(lineContexts, couponMatch, tenant, channel);
        BigDecimal actualCouponDiscount = allocateAndFlushToGroups(groups, lineContexts, couponMatch.getTotalCouponDiscount());
        totalCrossReduceDiscount = sumShopCrossReduceDiscount(groups);
        if (totalCrossReduceDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            clearCrossSelection(crossResult);
        }

        result.setTotalShopPromoDiscount(totalShopPromoDiscount);
        result.setTotalCrossReduceDiscount(totalCrossReduceDiscount);
        if (crossResult != null && crossResult.getSelectedActivityId() != null && totalCrossReduceDiscount.compareTo(BigDecimal.ZERO) > 0) {
            result.setSelectedCrossActivityId(crossResult.getSelectedActivityId());
            result.setSelectedCrossPromoType(crossResult.getSelectedPromoType());
            result.setSelectedCrossActivityTitle(crossResult.getSelectedActivityTitle());
            result.setSelectedCrossPlatformShareRate(crossResult.getPlatformShareRate());
            result.setSelectedCrossMerchantShareRate(crossResult.getMerchantShareRate());
        }
        result.setTotalCouponDiscount(actualCouponDiscount);
        result.setCouponSelector(couponMatch.getSelector());
        result.setShopGroups(groups);
        return result;
    }


    /**
     * 全场无进行中促销时：若用户仍锁定了店促/跨店，立即拦截
     */
    private void assertNoSpecifiedPromosWhenInactive(List<RemotePromoShopGroupBo> groups, Long specifiedCrossActivityId) {
        if (specifiedCrossActivityId != null && specifiedCrossActivityId != -1L) {
            throw new ServiceException("跨店优惠活动已变更或已失效，请返回重新结算");
        }
        boolean hasSpecifiedShopPromo = groups.stream()
            .filter(g -> CollUtil.isNotEmpty(g.getLines()))
            .flatMap(g -> g.getLines().stream())
            .anyMatch(line -> line.getSpecifiedShopActivityId() != null && line.getSpecifiedShopActivityId() > 0);
        if (hasSpecifiedShopPromo) {
            throw new ServiceException("店铺促销活动已变更或已失效，请返回重新结算");
        }
    }

    /**
     * 将各店及商品行重置为未参与任何促销的状态（折扣归零、选中态清空）
     */
    private void resetGroupsToNoPromoState(List<RemotePromoShopGroupBo> groups) {
        if (CollUtil.isEmpty(groups)) {
            return;
        }
        for (RemotePromoShopGroupBo group : groups) {
            group.setShopPromoDiscount(BigDecimal.ZERO);
            group.setShopCrossReduceDiscount(BigDecimal.ZERO);
            group.setShopPayAmount(group.getShopTotalAmount());
            group.setSelectedActivityId(null);
            group.setSelectedPromoType(null);
            group.setSelectedActivityTitle(null);
            if (CollUtil.isEmpty(group.getLines())) {
                continue;
            }
            for (RemotePromoLineItemBo line : group.getLines()) {
                line.setSelectedShopActivityId(null);
                line.setSelectedPromoType(null);
                line.setSelectedActivityTitle(null);
                line.setSelectedShopPlatformShareRate(null);
                line.setSelectedShopMerchantShareRate(null);
                line.setAvailablePromoOptions(Collections.emptyList());
                line.setGiftList(Collections.emptyList());
                line.setShopDiscountAmount(BigDecimal.ZERO);
            }
        }
    }

    private RemotePromoCalculateResult buildExclusiveResult(List<RemotePromoShopGroupBo> groups) {
        RemotePromoCalculateResult result = new RemotePromoCalculateResult();
        result.setTotalShopPromoDiscount(BigDecimal.ZERO);
        result.setTotalCrossReduceDiscount(BigDecimal.ZERO);
        result.setTotalCouponDiscount(BigDecimal.ZERO);
        RemoteCouponSelectorBo emptySelector = new RemoteCouponSelectorBo();
        emptySelector.setSelectedUserCouponId(null);
        emptySelector.setAvailableCoupons(Collections.emptyList());
        emptySelector.setDisabledCoupons(Collections.emptyList());
        result.setCouponSelector(emptySelector);

        for (RemotePromoShopGroupBo group : groups) {
            group.setShopPromoDiscount(BigDecimal.ZERO);
            group.setShopCrossReduceDiscount(BigDecimal.ZERO);
            group.setShopCouponDiscount(BigDecimal.ZERO);
            group.setShopPayAmount(group.getShopTotalAmount());
            if (CollUtil.isEmpty(group.getLines())) {
                continue;
            }
            for (RemotePromoLineItemBo line : group.getLines()) {
                clearLineDiscounts(line);
                line.setGiftList(Collections.emptyList());
                line.setAvailablePromoOptions(Collections.emptyList());
            }
        }
        result.setShopGroups(groups);
        return result;
    }

    private void clearLineDiscounts(RemotePromoLineItemBo line) {
        line.setShopDiscountAmount(BigDecimal.ZERO);
        line.setCrossDiscountAmount(BigDecimal.ZERO);
        line.setCouponDiscountAmount(BigDecimal.ZERO);
        line.setRealPayAmount(line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO);
    }

    /**
     * 秒杀/特价专享改价。
     * <p>
     * 校验活动有效性、类型、特价人群；再逐行校验活动 SKU 白名单、活动价、个人限购、活动库存；
     * 通过后将行单价改写为活动价并重算店小计/应付。不扣 Redis 库存（占库存在提交阶段）。
     * </p>
     */
    private void applyExclusivePromoPrice(List<RemotePromoShopGroupBo> groups, Long activityId, String tenant,
                                            String channel, Long userId, Long userLevelId, Date now) {
        // 1) 活动存在且在有效期内
        String actJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoDetail(tenant, channel, activityId));
        if (StringUtils.isBlank(actJson)) {
            throw new ServiceException("促销活动不存在或已结束");
        }
        PromoActivity activity = JsonUtils.parseObject(actJson, PromoActivity.class);
        if (activity == null || !inTimeWindow(activity.getStartTime(), activity.getEndTime(), now)) {
            throw new ServiceException("当前不在活动有效时间内");
        }
        // 2) 仅允许秒杀 / 特价；特价额外校验用户人群
        String promoType = activity.getPromoType();
        boolean isSeckill = TYPE_SECKILL.equalsIgnoreCase(promoType);
        boolean isSpecial = TYPE_SPECIAL.equalsIgnoreCase(promoType);
        if (!isSeckill && !isSpecial) {
            throw new ServiceException("指定活动非秒杀或特价活动");
        }
        if (isSpecial && !checkUserEligibility(activity.getUserScopeType(), activity.getUserParticipateType(),
            userId, userLevelId, UserScopeBiz.PROMO, activityId, tenant, channel)) {
            throw new ServiceException("您暂无参与该特价活动的权限");
        }
        for (RemotePromoShopGroupBo group : groups) {
            BigDecimal updatedShopTotal = BigDecimal.ZERO;
            for (RemotePromoLineItemBo item : group.getLines()) {
                // 商品必须在本活动 SKU 白名单中
                String skuPromoKey = isSeckill
                    ? PmCacheKeys.seckillSku(tenant, channel, activityId, item.getSkuId())
                    : PmCacheKeys.specialSku(tenant, channel, activityId, item.getSkuId());
                String skuPromoJson = stringRedisTemplate.opsForValue().get(skuPromoKey);
                if (StringUtils.isBlank(skuPromoJson)) {
                    throw new ServiceException("商品【" + item.getTitle() + "】未参与该活动");
                }
                PromoGoodsSku skuPromo = JsonUtils.parseObject(skuPromoJson, PromoGoodsSku.class);
                if (skuPromo == null || skuPromo.getPromoPrice() == null
                    || skuPromo.getPromoPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServiceException("商品【" + item.getTitle() + "】活动价格配置异常");
                }
                long userLimitQty = skuPromo.getUserLimitQty() != null ? skuPromo.getUserLimitQty() : -1L;
                long activityStock = skuPromo.getActivityStock() != null ? skuPromo.getActivityStock() : -1L;
                // 个人限购：已购 + 本次 ≤ 限购
                if (userId != null && userLimitQty > 0) {
                    String limitKey = isSeckill
                        ? PmCacheKeys.seckillUserLimit(tenant, channel, activityId, item.getSkuId())
                        : PmCacheKeys.specialUserLimit(tenant, channel, activityId, item.getSkuId());
                    Object boughtObj = stringRedisTemplate.opsForHash().get(limitKey, String.valueOf(userId));
                    int currentBought = boughtObj != null ? Integer.parseInt(boughtObj.toString()) : 0;
                    if ((currentBought + item.getBuyCount()) > userLimitQty) {
                        throw new ServiceException("商品【" + item.getTitle() + "】已达到限购上限，您已购 "
                            + currentBought + " 件，限购 " + userLimitQty + " 件");
                    }
                }
                // 活动库存预检
                if (activityStock != -1) {
                    String stockKey = isSeckill
                        ? PmCacheKeys.seckillStock(tenant, channel, activityId, item.getSkuId())
                        : PmCacheKeys.specialStock(tenant, channel, activityId, item.getSkuId());
                    String currentStockStr = stringRedisTemplate.opsForValue().get(stockKey);
                    if (currentStockStr == null) {
                        throw new ServiceException("商品【" + item.getTitle() + "】活动库存未就绪，请稍后重试或联系客服");
                    }
                        long remainingRealStock = Long.parseLong(currentStockStr);
                    if (remainingRealStock < item.getBuyCount()) {
                        throw new ServiceException("手慢了！商品【" + item.getTitle()
                                + "】活动专享库存不足，剩余仅 " + Math.max(0, remainingRealStock) + " 件");
                        }
                }

                // 改写为活动价并累加店小计
                item.setActualUnitPrice(skuPromo.getPromoPrice());
                item.setSubTotalAmount(skuPromo.getPromoPrice().multiply(BigDecimal.valueOf(item.getBuyCount())));
                updatedShopTotal = updatedShopTotal.add(item.getSubTotalAmount());
            }
            group.setShopTotalAmount(updatedShopTotal);
            group.setShopPayAmount(updatedShopTotal);
        }
    }

    /**
     * 同一店铺店促：每行至多选 1 个活动；同店不同行可选不同活动；选中同一活动的行装包算优惠再摊回行。
     * @return 本店店促优惠合计
     */
    private BigDecimal calculateShopPromoByPackages(RemotePromoShopGroupBo group, String tenant, String channel, Long userId, Long userLevelId, Date now, boolean cartPreview) {
        if (group == null) {
            return BigDecimal.ZERO;
        }
        if (CollUtil.isEmpty(group.getLines())) {
            group.setSelectedActivityId(null);
            group.setSelectedPromoType(null);
            group.setSelectedActivityTitle(null);
            return BigDecimal.ZERO;
        }
        List<RemotePromoLineItemBo> lines = group.getLines();
        for (RemotePromoLineItemBo line : lines) {
            line.setShopDiscountAmount(BigDecimal.ZERO);
            line.setGiftList(Collections.emptyList());
            prepareLineShopPromoSelection(line, tenant, channel, userId, userLevelId, now);
        }

        pickBestShopActivityForAutoLines(lines);

        // 把「选了同一个店促活动」的商品行归到一起。
        Map<Long, List<RemotePromoLineItemBo>> activityPackages = new LinkedHashMap<>();
        for (RemotePromoLineItemBo line : lines) {
            Long actId = line.getSelectedShopActivityId();
            if (actId == null || actId <= 0) {
                continue;
            }
            activityPackages.computeIfAbsent(actId, k -> new ArrayList<>()).add(line);
        }
        BigDecimal shopTotalDiscount = BigDecimal.ZERO;
        // 本店装包成功的活动摘要
        Map<Long, RemotePromoOptionBo> successPackByAct = new HashMap<>();
        for (Map.Entry<Long, List<RemotePromoLineItemBo>> entry : activityPackages.entrySet()) {
            Long actId = entry.getKey();
            List<RemotePromoLineItemBo> packLines = entry.getValue();
            PromoActivity activity = loadPromoActivity(tenant, channel, actId);
            BigDecimal actualPack = applyShopPackage(activity, packLines, actId, tenant, channel, userId, userLevelId, now);
            if (actualPack == null) {
                clearLineShopSelection(packLines);
                continue;
            }
            shopTotalDiscount = shopTotalDiscount.add(actualPack);
            String promoType = activity.getPromoType();
            String title = activity.getTitle();
            BigDecimal platformShareRate = activity.getPlatformShareRate();
            BigDecimal merchantShareRate = activity.getMerchantShareRate();
            for (RemotePromoLineItemBo line : packLines) {
                line.setSelectedPromoType(promoType);
                line.setSelectedActivityTitle(title);
                line.setSelectedShopPlatformShareRate(platformShareRate);
                line.setSelectedShopMerchantShareRate(merchantShareRate);
            }
            RemotePromoOptionBo packMeta = new RemotePromoOptionBo();
            packMeta.setActivityId(actId);
            packMeta.setPromoType(promoType);
            packMeta.setTitle(title);
            packMeta.setDiscountAmount(actualPack);
            packMeta.setPlatformShareRate(platformShareRate);
            packMeta.setMerchantShareRate(merchantShareRate);
            successPackByAct.put(actId, packMeta);
        }

        // 购物车预览每行活动列表
        if (!cartPreview) {
            for (RemotePromoLineItemBo line : lines) {
                line.setAvailablePromoOptions(Collections.emptyList());
            }
        }

        if (successPackByAct.size() == 1) {
            RemotePromoOptionBo only = successPackByAct.values().iterator().next();
            group.setSelectedActivityId(only.getActivityId());
            group.setSelectedPromoType(only.getPromoType());
            group.setSelectedActivityTitle(only.getTitle());
        } else {
            group.setSelectedActivityId(null);
            group.setSelectedPromoType(null);
            group.setSelectedActivityTitle(null);
        }
        return shopTotalDiscount;
    }

    /**
     * 行级可选店促
     */
    private void prepareLineShopPromoSelection(RemotePromoLineItemBo line, String tenant, String channel, Long userId, Long userLevelId, Date now) {
        Long specified = line.getSpecifiedShopActivityId();
        // 不用店促直接返回
        if (specified != null && specified == -1L) {
            line.setSelectedShopActivityId(null);
            line.setSelectedPromoType(null);
            line.setSelectedActivityTitle(null);
            line.setSelectedShopPlatformShareRate(null);
            line.setSelectedShopMerchantShareRate(null);
            line.setAvailablePromoOptions(Collections.emptyList());
            line.setGiftList(Collections.emptyList());
            return;
        }
        List<RemotePromoOptionBo> options = listLineShopPromoOptions(line, tenant, channel, userId, userLevelId, now);
        line.setAvailablePromoOptions(options);
        if (specified != null && specified > 0) {
            RemotePromoOptionBo locked = options.stream()
                .filter(o -> specified.equals(o.getActivityId()))
                .findFirst()
                .orElse(null);
            if (locked == null) {
                throwSpecifiedShopPromoUnavailable(specified, tenant, channel, userId);
            }
            line.setSelectedShopActivityId(locked.getActivityId());
            line.setSelectedPromoType(locked.getPromoType());
            line.setSelectedActivityTitle(locked.getTitle());
            line.setSelectedShopPlatformShareRate(locked.getPlatformShareRate());
            line.setSelectedShopMerchantShareRate(locked.getMerchantShareRate());
            for (RemotePromoOptionBo opt : options) {
                opt.setIsBest(opt.getActivityId().equals(locked.getActivityId()));
            }
            return;
        }
        // 自动选：先不选，等本行从 options 择优
        line.setSelectedShopActivityId(null);
        line.setSelectedPromoType(null);
        line.setSelectedActivityTitle(null);
        line.setSelectedShopPlatformShareRate(null);
        line.setSelectedShopMerchantShareRate(null);
    }

    /**
     * 锁定店促不在选项中：优先区分「参与次数用尽」与「活动失效」。
     */
    private void throwSpecifiedShopPromoUnavailable(Long specifiedActId, String tenant, String channel, Long userId) {
        PromoActivity activity = loadPromoActivity(tenant, channel, specifiedActId);
        if (activity != null && !promoUserQuotaService.hasRemainingJoinQuota(tenant, channel, userId, activity)) {
            throw new ServiceException("您已达到该店铺促销活动的参与次数上限，请返回重新结算");
        }
        throw new ServiceException("店铺促销活动已变更或已失效，请返回重新结算");
    }

    /**
     * 给没有指定店促的商品行，从本行可选活动里自动挑一个默认挂上。
     * *  1. 优惠金额降序（越大越好）
     *  * 2. 是否有赠品降序（有赠品优先）
     *  * 3. 活动ID升序（更早创建/更小ID优先，保证确定性）
     */
    private static final Comparator<RemotePromoOptionBo> OPTION_PRIORITY_COMPARATOR = Comparator
        .comparing((RemotePromoOptionBo o) -> Optional.ofNullable(o.getDiscountAmount()).orElse(BigDecimal.ZERO), Comparator.reverseOrder())
        .thenComparing(o -> CollUtil.isNotEmpty(o.getGiftList()), Comparator.reverseOrder())
        .thenComparing(RemotePromoOptionBo::getActivityId);

    /**
     * 跨店自动选优：
     * 1. 优惠金额降序（越大越好）
     * 2. 金额相同时，取更小的活动 ID
     */
    private static final Comparator<CrossCandidate> CROSS_CANDIDATE_COMPARATOR = Comparator
        .comparing(CrossCandidate::discountAmount, Comparator.reverseOrder())
        .thenComparing(CrossCandidate::activityId);

    /**
     * 跨店试算成功后的临时结果包
     */
    private record CrossCandidate(Long activityId, String promoType, String title, BigDecimal discountAmount,
                                  BigDecimal platformShareRate, BigDecimal merchantShareRate) {
    }

    private void pickBestShopActivityForAutoLines(List<RemotePromoLineItemBo> lines) {
        if (CollUtil.isEmpty(lines)) {
            return;
        }

        for (RemotePromoLineItemBo line : lines) {
            // 锁定或者没有可选店促不参与自动择优
            if (line.getSpecifiedShopActivityId() != null || CollUtil.isEmpty(line.getAvailablePromoOptions())) {
                continue;
            }
            RemotePromoOptionBo best = line.getAvailablePromoOptions().stream()
                .filter(opt -> opt != null && opt.getActivityId() != null)
                .min(OPTION_PRIORITY_COMPARATOR)
                .orElse(null);
            if (best == null) {
                continue;
            }
            line.setSelectedShopActivityId(best.getActivityId());
            line.setSelectedPromoType(best.getPromoType());
            line.setSelectedActivityTitle(best.getTitle());
            line.setSelectedShopPlatformShareRate(best.getPlatformShareRate());
            line.setSelectedShopMerchantShareRate(best.getMerchantShareRate());
            for (RemotePromoOptionBo opt : line.getAvailablePromoOptions()) {
                if (opt != null && opt.getActivityId() != null) {
                    opt.setIsBest(best.getActivityId().equals(opt.getActivityId()));
                }
            }
        }
    }

    /**
     * 对「同一个店促活动 + 选中了它的那几行」做一次装包履约：判断这个活动包能不能用；能用就当场把优惠或赠品落到行上，并把结果告诉外层。
     * @param activity  活动详情
     * @param packLines 本店选中该活动的商品行
     * @param actId     活动 ID
     */
    private BigDecimal applyShopPackage(PromoActivity activity, List<RemotePromoLineItemBo> packLines,
                                        Long actId, String tenant, String channel,
                                        Long userId, Long userLevelId, Date now) {
        // 活动存在、有效期、人群/等级；包内须有行
        if (activity == null || CollUtil.isEmpty(packLines)
            || !inTimeWindow(activity.getStartTime(), activity.getEndTime(), now)
            || !checkUserEligibility(activity.getUserScopeType(), activity.getUserParticipateType(),
            userId, userLevelId, UserScopeBiz.PROMO, actId, tenant, channel)) {
            return null;
        }
        // 活动级每人参与次数：锁定超限抛错，自动选则装包失败静默清促
        if (!promoUserQuotaService.hasRemainingJoinQuota(tenant, channel, userId, activity)) {
            boolean locked = packLines.stream()
                .anyMatch(l -> actId.equals(l.getSpecifiedShopActivityId()));
            if (locked) {
                throw new ServiceException("您已达到该店铺促销活动的参与次数上限，请返回重新结算");
            }
            return null;
        }

        String promoType = activity.getPromoType();
        List<RemotePromoLineItemBo> eligibleLines = collectEligiblePackLines(activity, packLines, tenant, channel, actId);
        EligibleItemMetrics metrics = sumEligiblePackMetrics(eligibleLines);

        // 满减 / 满折：算阶梯优惠 → 按合资格行小计占比摊到行
        if (TYPE_FULL_REDUCE.equalsIgnoreCase(promoType) || TYPE_FULL_DISCOUNT.equalsIgnoreCase(promoType)) {
            if (metrics.getAmount().compareTo(BigDecimal.ZERO) <= 0 || metrics.getQuantity() <= 0) {
                return null;
            }
            BigDecimal packDiscount = calculateTierDiscount(activity, metrics.getAmount(), metrics.getQuantity(), tenant, channel);
            if (packDiscount == null || packDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            return allocatePackageDiscountToLines(eligibleLines, packDiscount);
        }

        // 满赠：不减钱，达标则把赠品挂到合资格行
        if (TYPE_FULL_GIFT.equalsIgnoreCase(promoType)) {
            List<RemotePromoGiftItemBo> packGifts = matchTierGifts(actId, activity, metrics.getAmount(), metrics.getQuantity(), tenant, channel);
            if (CollUtil.isEmpty(packGifts)) {
                return null;
            }
            for (RemotePromoLineItemBo line : packLines) {
                line.setGiftList(Collections.emptyList());
            }
            eligibleLines.forEach(line -> line.setGiftList(packGifts));
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * 从一个店促活动包里，筛出真正能参与门槛和分摊的商品行
     */
    private List<RemotePromoLineItemBo> collectEligiblePackLines(PromoActivity activity, List<RemotePromoLineItemBo> packLines,
                                                                 String tenant, String channel, Long actId) {
        List<RemotePromoLineItemBo> eligibleLines = new ArrayList<>();
        if (CollUtil.isEmpty(packLines)) {
            return eligibleLines;
        }
        PromoItemScopeContext ctx = buildPromoItemScopeContext(activity, tenant, channel, actId);
        for (RemotePromoLineItemBo line : packLines) {
            BigDecimal sub = line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO;
            // 不在活动圈货内，或行小计 ≤ 0 不进合资格列表
            if (!isProductEligibleForPromo(line, ctx) || sub.compareTo(BigDecimal.ZERO) <= 0) {
                line.setShopDiscountAmount(BigDecimal.ZERO);
                continue;
            }
            eligibleLines.add(line);
        }
        return eligibleLines;
    }

    /**
     * 把已经圈好的合资格行 加起来多少钱、多少件
     */
    private EligibleItemMetrics sumEligiblePackMetrics(List<RemotePromoLineItemBo> eligibleLines) {
        EligibleItemMetrics metrics = new EligibleItemMetrics();
        if (CollUtil.isEmpty(eligibleLines)) {
            return metrics;
        }
        for (RemotePromoLineItemBo line : eligibleLines) {
            metrics.setAmount(metrics.getAmount().add(
                line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO));
            metrics.setQuantity(metrics.getQuantity() + (line.getBuyCount() != null ? line.getBuyCount() : 0));
        }
        return metrics;
    }

    private void clearLineShopSelection(List<RemotePromoLineItemBo> packLines) {
        for (RemotePromoLineItemBo line : packLines) {
            line.setSelectedShopActivityId(null);
            line.setSelectedPromoType(null);
            line.setSelectedActivityTitle(null);
            line.setSelectedShopPlatformShareRate(null);
            line.setSelectedShopMerchantShareRate(null);
            line.setShopDiscountAmount(BigDecimal.ZERO);
            line.setGiftList(Collections.emptyList());
        }
    }

    /**
     * 按这行的 SKU/类目/品牌，从缓存召回可能相关的活动，过滤掉无效/非店促/用户不符/圈货不合的，留下满减/满折/满赠选项（门槛未达也可以进列表）。
     */
    private List<RemotePromoOptionBo> listLineShopPromoOptions(RemotePromoLineItemBo line, String tenant, String channel, Long userId, Long userLevelId, Date now) {
        List<RemotePromoOptionBo> options = new ArrayList<>();
        // 1) 按 SKU/类目/品牌倒排，召回可能命中的活动 ID
        Set<String> candidateActIds = recallPromoCandidateIds(tenant, channel,
            line.getSkuId(), line.getCategoryAncestorIds(), line.getBrandId());
        if (CollUtil.isEmpty(candidateActIds)) {
            return options;
        }
        // 2) 批量拉活动详情
        List<String> idList = new ArrayList<>(candidateActIds);
        List<String> detailKeys = idList.stream()
            .map(id -> PmCacheKeys.promoDetail(tenant, channel, Long.valueOf(id)))
            .collect(Collectors.toList());
        List<String> actJsons = stringRedisTemplate.opsForValue().multiGet(detailKeys);
        List<RemotePromoLineItemBo> single = List.of(line);
        for (int i = 0; i < idList.size(); i++) {
            String json = actJsons != null ? actJsons.get(i) : null;
            if (StringUtils.isBlank(json)) {
                continue;
            }
            PromoActivity activity = JsonUtils.parseObject(json, PromoActivity.class);
            if (activity == null || activity.getId() == null) {
                continue;
            }
            // 不在有效期内
            if (!inTimeWindow(activity.getStartTime(), activity.getEndTime(), now)) {
                continue;
            }
            String promoType = activity.getPromoType();
            if (TYPE_SECKILL.equalsIgnoreCase(promoType) || TYPE_SPECIAL.equalsIgnoreCase(promoType)
                || TYPE_CROSS_FULL_REDUCE.equalsIgnoreCase(promoType)
                || TYPE_CROSS_FULL_DISCOUNT.equalsIgnoreCase(promoType)) {
                continue;
            }
            Long actId = activity.getId();
            // 用户人群/等级不满足
            if (!checkUserEligibility(activity.getUserScopeType(), activity.getUserParticipateType(),
                userId, userLevelId, UserScopeBiz.PROMO, actId, tenant, channel)) {
                continue;
            }
            // 活动级每人参与次数已满：不进选项
            if (!promoUserQuotaService.hasRemainingJoinQuota(tenant, channel, userId, activity)) {
                continue;
            }
            // 本行是否在圈货范围内（合资格金额/件数 > 0）；门槛不在此拦截
            EligibleItemMetrics metrics = calculateEligibleMetricsForPromo(
                activity, single, tenant, channel, actId, AMOUNT_SUBTOTAL);
            if (metrics.getAmount().compareTo(BigDecimal.ZERO) <= 0 || metrics.getQuantity() <= 0) {
                continue;
            }
            if (TYPE_FULL_REDUCE.equalsIgnoreCase(promoType) || TYPE_FULL_DISCOUNT.equalsIgnoreCase(promoType)) {
                BigDecimal discount = calculateTierDiscount(activity, metrics.getAmount(), metrics.getQuantity(), tenant, channel);
                    RemotePromoOptionBo opt = new RemotePromoOptionBo();
                    opt.setActivityId(actId);
                    opt.setPromoType(promoType);
                    opt.setTitle(activity.getTitle());
                opt.setDiscountAmount(discount != null ? discount : BigDecimal.ZERO);
                opt.setPlatformShareRate(activity.getPlatformShareRate());
                opt.setMerchantShareRate(activity.getMerchantShareRate());
                    options.add(opt);
            } else if (TYPE_FULL_GIFT.equalsIgnoreCase(promoType)) {
                List<RemotePromoGiftItemBo> giftList = matchTierGifts(actId, activity, metrics.getAmount(), metrics.getQuantity(), tenant, channel);
                    RemotePromoOptionBo opt = new RemotePromoOptionBo();
                    opt.setActivityId(actId);
                    opt.setPromoType(TYPE_FULL_GIFT);
                    opt.setTitle(activity.getTitle());
                    opt.setDiscountAmount(BigDecimal.ZERO);
                opt.setGiftList(CollUtil.isNotEmpty(giftList) ? giftList : Collections.emptyList());
                opt.setPlatformShareRate(activity.getPlatformShareRate());
                opt.setMerchantShareRate(activity.getMerchantShareRate());
                    options.add(opt);
                }
            }
        return options;
    }

    /**
     * 把包上要减的总优惠，按各行小计比例拆到已圈好的行上，并返回实际减了多少。
     * @param eligibleLines 已圈好的合资格行
     * @param packDiscount  包待分摊总额
     */
    private BigDecimal allocatePackageDiscountToLines(List<RemotePromoLineItemBo> eligibleLines, BigDecimal packDiscount) {
        if (CollUtil.isEmpty(eligibleLines) || packDiscount == null || packDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            if (CollUtil.isNotEmpty(eligibleLines)) {
                for (RemotePromoLineItemBo line : eligibleLines) {
                    line.setShopDiscountAmount(BigDecimal.ZERO);
                }
            }
            return BigDecimal.ZERO;
        }

        // 先清零，避免基数≤0 的行残留旧值；再按小计就地分摊
        for (RemotePromoLineItemBo line : eligibleLines) {
            line.setShopDiscountAmount(BigDecimal.ZERO);
        }
        PromoObjectAllocator.allocate(
            eligibleLines,
            line -> line.getSubTotalAmount() != null ? line.getSubTotalAmount() : BigDecimal.ZERO,
            RemotePromoLineItemBo::setShopDiscountAmount,
            packDiscount);

        BigDecimal actualTotal = BigDecimal.ZERO;
        for (RemotePromoLineItemBo line : eligibleLines) {
            if (line.getShopDiscountAmount() != null) {
                actualTotal = actualTotal.add(line.getShopDiscountAmount());
            }
        }
        return actualTotal;
    }

    /**
     * 整单只选一个跨店满减/满折，算出能减多少 还没摊到行
     *
     */
    private CrossPromoCalculationResult calculateCrossPromoMutex(List<RemotePromoShopGroupBo> groups,
                                                                 BigDecimal totalBaseAmount, String tenant, String channel,
                                                                 Long userId, Long userLevelId, Date now,
                                                                 Long specifiedCrossActivityId) {
        CrossPromoCalculationResult result = new CrossPromoCalculationResult();
        boolean lockSelection = specifiedCrossActivityId != null;
        Long lockedActId = (specifiedCrossActivityId != null && specifiedCrossActivityId == -1L) ? null : specifiedCrossActivityId;
        if (totalBaseAmount == null || totalBaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            if (lockSelection && lockedActId != null) {
                throw new ServiceException("跨店优惠活动已变更或已失效，请返回重新结算");
            }
            return result;
        }
        // 明确不用跨店
        if (lockSelection && lockedActId == null) {
            zeroShopCross(groups);
            return result;
        }

        List<String> targetIds;
        if (lockSelection) {
            targetIds = Collections.singletonList(String.valueOf(lockedActId));
        } else {
            Set<String> actIds = recallCartPromoCandidateIds(tenant, channel, groups);
        if (CollUtil.isEmpty(actIds)) {
            return result;
        }
            targetIds = new ArrayList<>(actIds);
        }
        List<String> detailKeys = targetIds.stream()
            .map(id -> PmCacheKeys.promoDetail(tenant, channel, Long.valueOf(id)))
            .collect(Collectors.toList());
        List<String> actJsons = stringRedisTemplate.opsForValue().multiGet(detailKeys);
        if (CollUtil.isEmpty(actJsons) || actJsons.stream().allMatch(StringUtils::isBlank)) {
            if (lockSelection) {
                throw new ServiceException("跨店优惠活动已变更或已失效，请返回重新结算");
            }
            return result;
        }

        List<RemotePromoLineItemBo> allLines = flattenProducts(groups);
        // 当前订单真正能用的跨店候选池
        List<CrossCandidate> availableCandidates = new ArrayList<>();
        for (String json : actJsons) {
            if (StringUtils.isBlank(json)) {
                continue;
            }
            PromoActivity activity = JsonUtils.parseObject(json, PromoActivity.class);
            CrossCandidate candidate = trialSingleCrossPromo(activity, allLines, tenant, channel, userId, userLevelId, now);
            if (candidate != null) {
                availableCandidates.add(candidate);
            }
        }

        CrossCandidate bestCandidate = null;
        if (lockSelection) {
            // 用户锁定了某个跨店，从候选池中挑选，不存在则报错
            bestCandidate = availableCandidates.stream()
                .filter(c -> lockedActId.equals(c.activityId()))
                .findFirst()
                .orElse(null);
            if (bestCandidate == null) {
                PromoActivity lockedAct = loadPromoActivity(tenant, channel, lockedActId);
                if (lockedAct != null
                    && !promoUserQuotaService.hasRemainingJoinQuota(tenant, channel, userId, lockedAct)) {
                    throw new ServiceException("您已达到该跨店优惠活动的参与次数上限，请返回重新结算");
                }
                throw new ServiceException("跨店优惠活动已变更或已失效，请返回重新结算");
            }
        } else if (CollUtil.isNotEmpty(availableCandidates)) {
            bestCandidate = availableCandidates.stream()
                .min(CROSS_CANDIDATE_COMPARATOR)
                .orElse(null);
        }
        zeroShopCross(groups);
        if (bestCandidate != null && bestCandidate.discountAmount().compareTo(BigDecimal.ZERO) > 0) {
            result.setDiscountAmount(bestCandidate.discountAmount());
            result.setSelectedActivityId(bestCandidate.activityId());
            result.setSelectedPromoType(bestCandidate.promoType());
            result.setSelectedActivityTitle(bestCandidate.title());
            result.setPlatformShareRate(bestCandidate.platformShareRate());
            result.setMerchantShareRate(bestCandidate.merchantShareRate());
        }
        return result;
    }

    /**
     * 对一个跨店活动做完整试算：能不能用、实际能减多少。
     */
    private CrossCandidate trialSingleCrossPromo(PromoActivity activity, List<RemotePromoLineItemBo> allLines,
                                                 String tenant, String channel, Long userId, Long userLevelId, Date now) {
            if (activity == null || activity.getId() == null) {
            return null;
            }
        // 不在有效期内
            if (!inTimeWindow(activity.getStartTime(), activity.getEndTime(), now)) {
            return null;
            }
        // 只认跨店满减 / 跨店满折
            String promoType = activity.getPromoType();
        if (!TYPE_CROSS_FULL_REDUCE.equalsIgnoreCase(promoType) && !TYPE_CROSS_FULL_DISCOUNT.equalsIgnoreCase(promoType)) {
            return null;
            }

            Long actId = activity.getId();
        // 用户人群 / 等级
            if (!checkUserEligibility(activity.getUserScopeType(), activity.getUserParticipateType(),
                userId, userLevelId, UserScopeBiz.PROMO, actId, tenant, channel)) {
            return null;
        }
        if (!promoUserQuotaService.hasRemainingJoinQuota(tenant, channel, userId, activity)) {
            return null;
        }
        EligibleItemMetrics metrics = calculateEligibleMetricsForPromo(activity, allLines, tenant, channel, actId, AMOUNT_REMAIN_AFTER_SHOP);
        if (metrics.getAmount().compareTo(BigDecimal.ZERO) <= 0 || metrics.getQuantity() <= 0) {
            return null;
        }

        BigDecimal discount = calculateTierDiscount(activity, metrics.getAmount(), metrics.getQuantity(), tenant, channel);
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        // 跨店实际能减多少，不能超过还能减的钱
        BigDecimal applicable = discount.min(metrics.getAmount());
        if (applicable.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        // 平台预算（分）：有配置则校验剩余预算是否够扣
        String budgetVal = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoBudget(tenant, channel, actId));
            if (budgetVal != null) {
                long remainingBudgetFen = Long.parseLong(budgetVal);
            // 本次扣减的金额大于预算，则提示
            if (remainingBudgetFen <= 0 || MoneyFenUtils.toFen(applicable) > remainingBudgetFen) {
                log.debug("跨店活动预算不足 activityId={}, applicable={}, remainingFen={}",
                    actId, applicable, remainingBudgetFen);
                return null;
            }
        }
        return new CrossCandidate(actId, promoType, activity.getTitle(), applicable,
            activity.getPlatformShareRate(), activity.getMerchantShareRate());
    }

    private void zeroShopCross(List<RemotePromoShopGroupBo> groups) {
        if (CollUtil.isEmpty(groups)) {
            return;
        }
        for (RemotePromoShopGroupBo group : groups) {
            group.setShopCrossReduceDiscount(BigDecimal.ZERO);
        }
    }

    private List<RemotePromoLineItemBo> flattenProducts(List<RemotePromoShopGroupBo> groups) {
        List<RemotePromoLineItemBo> result = new ArrayList<>();
        if (CollUtil.isEmpty(groups)) {
            return result;
        }
        for (RemotePromoShopGroupBo group : groups) {
            if (CollUtil.isNotEmpty(group.getLines())) {
                result.addAll(group.getLines());
            }
        }
        return result;
    }

    /**
     * 统计某活动在商品列表中的合资格金额与件数。
     * 有效金额 ≤ 0 的行不计入金额，也不凑件数。
     */
    private EligibleItemMetrics calculateEligibleMetricsForPromo(PromoActivity activity, List<RemotePromoLineItemBo> productList, String tenant, String channel, Long actId,
        Function<RemotePromoLineItemBo, BigDecimal> amountExtractor) {
        EligibleItemMetrics metrics = new EligibleItemMetrics();
        if (CollUtil.isEmpty(productList) || amountExtractor == null) {
            return metrics;
        }
        PromoItemScopeContext ctx = buildPromoItemScopeContext(activity, tenant, channel, actId);
        for (RemotePromoLineItemBo item : productList) {
            if (!isProductEligibleForPromo(item, ctx)) {
                continue;
            }
            BigDecimal effectiveAmount = amountExtractor.apply(item);
            if (effectiveAmount == null || effectiveAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            metrics.setAmount(metrics.getAmount().add(effectiveAmount));
            metrics.setQuantity(metrics.getQuantity() + (item.getBuyCount() != null ? item.getBuyCount() : 0));
        }
        return metrics;
    }

    /**
     * 按活动阶梯规则计算满减/满折优惠金额
     *   <li>阶梯按门槛从高到低排序，命中第一档即返回（高门槛优先）</li>
     *   <li>满减：{@code isCap=1} 为每满循环（floor(基数/门槛)×减免额），否则固定减免</li>
     *   <li>满折：{@code benefit} 为折扣率（如 0.8 表示 8 折），优惠额 = 合资格金额 × (1 - benefit)</li>
     * </ul>
     *
     * @param totalAmount 合资格商品小计
     * @param totalQty    合资格商品件数
     * @return 优惠金额；未达门槛或无阶梯时为 0
     */
    private BigDecimal calculateTierDiscount(PromoActivity activity, BigDecimal totalAmount,
                                             int totalQty, String tenant, String channel) {
        Long actId = activity.getId();
        // 读活动阶梯 JSON
        String tiersJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoTiers(tenant, channel, actId));
        if (StringUtils.isBlank(tiersJson)) {
            return BigDecimal.ZERO;
        }
        List<PromoTierRule> tiers;
        try {
            tiers = JsonUtils.parseArray(tiersJson, PromoTierRule.class);
        } catch (Exception e) {
            log.debug("解析促销阶梯失败 activityId={}", actId);
            return BigDecimal.ZERO;
        }
        if (CollUtil.isEmpty(tiers)) {
            return BigDecimal.ZERO;
        }
        // 高门槛优先，命中即停
        tiers.sort((o1, o2) -> o2.getThresholdValue().compareTo(o1.getThresholdValue()));
        int calcType = activity.getCalcType() == null ? 1 : activity.getCalcType().intValue();
        String promoType = activity.getPromoType();
        int isCap = activity.getIsCap() == null ? 0 : activity.getIsCap().intValue();
        boolean isLoopMode = (isCap == 1);
        // 门槛比较基数：按件 or 按金额
        BigDecimal compareBase = (calcType == 2) ? BigDecimal.valueOf(totalQty) : totalAmount;
        for (PromoTierRule tier : tiers) {
            BigDecimal threshold = tier.getThresholdValue();
            BigDecimal benefit = tier.getBenefitValue();
            if (threshold == null || benefit == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (compareBase.compareTo(threshold) >= 0) {
                // 满减 / 跨店满减
                if (TYPE_FULL_REDUCE.equalsIgnoreCase(promoType)
                    || TYPE_CROSS_FULL_REDUCE.equalsIgnoreCase(promoType)) {
                    // 上不封顶：每满循环
                    if (isLoopMode) {
                        // 循环次数 = 比较基数 / 门槛
                        long count = compareBase.divide(threshold, 0, RoundingMode.FLOOR).longValue();
                        // 优惠 = 优惠金额 * 循环次数
                        return benefit.multiply(BigDecimal.valueOf(count));
                    }
                    // 不是上不封顶，直接返回固定金额
                    return benefit;
                } else if (TYPE_FULL_DISCOUNT.equalsIgnoreCase(promoType)
                    || TYPE_CROSS_FULL_DISCOUNT.equalsIgnoreCase(promoType)) {
                    // 满折：优惠额 = 合资格金额 × (1 - 折扣率)
                    return totalAmount.multiply(BigDecimal.ONE.subtract(benefit))
                        .setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        // 未达任何门槛
        return BigDecimal.ZERO;
    }

    /**
     * 判断当前活动是否达到满赠门槛；达到则返回可送的赠品列表
     * <p>
     * 当前取阶梯列表第一档；未达门槛、无赠品或赠品不可售时返回空列表。
     * </p>
     *
     * @param totalAmount 合资格商品小计
     * @param totalQty    合资格商品件数
     */
    private List<RemotePromoGiftItemBo> matchTierGifts(Long actId, PromoActivity activity, BigDecimal totalAmount,
                                                       int totalQty, String tenant, String channel) {
        // 读缓存阶梯
        String tiersJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoTiers(tenant, channel, actId));
        if (StringUtils.isBlank(tiersJson)) {
            return Collections.emptyList();
        }
        List<PromoTierRule> tiers;
        try {
            tiers = JsonUtils.parseArray(tiersJson, PromoTierRule.class);
        } catch (Exception e) {
            log.debug("解析满赠阶梯失败 activityId={}", actId);
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(tiers)) {
            return Collections.emptyList();
        }

        // 当前只使用第一档门槛
        PromoTierRule tier = tiers.get(0);
        BigDecimal threshold = tier.getThresholdValue();
        if (threshold == null) {
            return Collections.emptyList();
        }
        int calcType = activity.getCalcType() == null ? 1 : activity.getCalcType().intValue();
        BigDecimal compareBase = (calcType == 2) ? BigDecimal.valueOf(totalQty) : totalAmount;
        if (compareBase.compareTo(threshold) < 0) {
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(tier.getGiftList())) {
            return Collections.emptyList();
        }
        // 补全赠品标题/主图等展示信息，过滤不可售
        List<RemotePromoGiftItemBo> gifts = enrichGiftItems(tier.getGiftList(), tenant);
        if (CollUtil.isEmpty(gifts)) {
            return Collections.emptyList();
        }
        for (RemotePromoGiftItemBo gift : gifts) {
            if (gift.getTierRuleId() == null) {
                gift.setTierRuleId(tier.getId());
            }
        }
        return gifts;
    }

    /**
     把满赠阶梯里配置的「赠品 SKU + 数量」，查成可展示、可送出的赠品清单。任一赠品不存在、不可售或库存不够 → 整单满赠作废（返回空列表）。
     */
    private List<RemotePromoGiftItemBo> enrichGiftItems(List<PromoTierGift> giftDetails, String tenantId) {
        // 批量查商品
        List<String> skuIds = giftDetails.stream()
            .map(PromoTierGift::getGiftSkuId)
            .filter(id -> id != null)
            .map(String::valueOf)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, RemoteMasterSkuBo> goodsMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuIds)) {
            try {
                List<RemoteMasterSkuBo> goodsList = remoteGoodsService.selectBySkuIds(tenantId, skuIds);
                if (CollUtil.isNotEmpty(goodsList)) {
                    for (RemoteMasterSkuBo g : goodsList) {
                        if (g.getSkuId() != null) {
                            goodsMap.put(g.getSkuId(), g);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("结算页查询满赠商品信息失败 tenantId={}", tenantId, e);
                return Collections.emptyList();
            }
        }

        List<RemotePromoGiftItemBo> result = new ArrayList<>();
        for (PromoTierGift detail : giftDetails) {
            if (detail.getGiftSkuId() == null) {
                continue;
            }
            int need = detail.getGiftCount() != null ? detail.getGiftCount().intValue() : 1;
            RemoteMasterSkuBo goods = goodsMap.get(detail.getGiftSkuId());
            if (goods == null) {
                log.info("满赠赠品不存在或已下架 giftSkuId={}", detail.getGiftSkuId());
                return Collections.emptyList();
            }
            // 赠品行的商品是否上架且审核通过
            if (!isSkuSaleable(goods)) {
                log.info("满赠赠品不可售 giftSkuId={} title={}", detail.getGiftSkuId(), goods.getTitle());
                return Collections.emptyList();
            }
            int stock = goods.getOriginStock() != null ? goods.getOriginStock() : 0;
            if (stock < need) {
                log.info("满赠赠品库存不足 giftSkuId={} need={} stock={}", detail.getGiftSkuId(), need, stock);
                return Collections.emptyList();
            }
            RemotePromoGiftItemBo item = new RemotePromoGiftItemBo();
            item.setGiftId(detail.getId());
            item.setGiftSkuId(detail.getGiftSkuId());
            item.setGiftCount(need);
            item.setSpuId(goods.getSpuId());
            item.setTitle(goods.getTitle());
            item.setMainPicture(goods.getCoverImage());
            item.setSpecText(goods.getSpecText());
            item.setSkuCode(StringUtils.blankToDefault(goods.getSkuCode(), ""));
            result.add(item);
        }
        return result;
    }

    private boolean isSkuSaleable(RemoteMasterSkuBo goods) {
        if (goods == null) {
            return false;
        }
        Integer saleStatus = goods.getSkuSaleStatus();
        Integer auditStatus = goods.getAuditStatus();
        return saleStatus != null && saleStatus == SKU_SALE_ON
            && auditStatus != null && auditStatus == AUDIT_PASSED;
    }

    /**
     * 按商品维度从 Redis 倒排索引召回可能命中的促销活动 ID。
     */
    private Set<String> recallPromoCandidateIds(String tenant, String channel, Long skuId,
                                                List<Long> categoryAncestorIds, Long brandId) {
        Set<String> candidateIds = new HashSet<>();
        // 全场（不限商品范围）活动
        Set<String> allScope = stringRedisTemplate.opsForSet().members(PmCacheKeys.promoScopeAll(tenant, channel));
        if (CollUtil.isNotEmpty(allScope)) {
            candidateIds.addAll(allScope);
        }
        // 指定 SKU 白名单活动
        if (skuId != null) {
            Set<String> skuScope = stringRedisTemplate.opsForSet().members(
                PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_SKU, skuId));
            if (CollUtil.isNotEmpty(skuScope)) {
                candidateIds.addAll(skuScope);
            }
        }
        // 类目白名单（含祖先路径，父类目活动可命中子类商品）
        if (CollUtil.isNotEmpty(categoryAncestorIds)) {
            for (Long catId : categoryAncestorIds) {
                if (catId != null) {
                    Set<String> catScope = stringRedisTemplate.opsForSet().members(
                        PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_CATEGORY, catId));
                    if (CollUtil.isNotEmpty(catScope)) {
                        candidateIds.addAll(catScope);
                    }
                }
            }
        }
        // 品牌白名单活动
        if (brandId != null) {
            Set<String> brandScope = stringRedisTemplate.opsForSet().members(
                PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_BRAND, brandId));
            if (CollUtil.isNotEmpty(brandScope)) {
                candidateIds.addAll(brandScope);
            }
        }
        return candidateIds;
    }

    /**
     * 按本单全部商品行召回促销候选。
     */
    private Set<String> recallCartPromoCandidateIds(String tenant, String channel, List<RemotePromoShopGroupBo> groups) {
        Set<String> candidateIds = new HashSet<>();
        for (RemotePromoLineItemBo line : flattenProducts(groups)) {
            if (line == null) {
                continue;
            }
            Set<String> lineCandidates = recallPromoCandidateIds(tenant, channel, line.getSkuId(), line.getCategoryAncestorIds(), line.getBrandId());
            if (CollUtil.isNotEmpty(lineCandidates)) {
                candidateIds.addAll(lineCandidates);
            }
        }
        if (CollUtil.isEmpty(candidateIds)) {
            Set<String> allScope = stringRedisTemplate.opsForSet().members(PmCacheKeys.promoScopeAll(tenant, channel));
            if (CollUtil.isNotEmpty(allScope)) {
                candidateIds.addAll(allScope);
            }
        }
        return candidateIds;
    }

    /**
     * 校验当前用户是否满足活动/券/赠券规则的人群范围。
     * <p>
     * 口径：
     * <ul>
     *   <li>{@code userScopeType}：0/空=不限；1=指定用户；2=指定会员等级</li>
     *   <li>{@code userParticipateType}：仅对指定用户生效——1/空=白名单，2=黑名单；指定等级只有白名单</li>
     *   <li>{@code biz}：PROMO / COUPON / GRANT，决定读哪套 Redis 名单 key</li>
     * </ul>
     * </p>
     *
     * @return true=可参与；false=不可参与
     */
    private boolean checkUserEligibility(Long userScopeType, Long userParticipateType,
                                         Long userId, Long userLevelId,
                                         UserScopeBiz biz, Long bizId,
                                         String tenant, String channel) {
        // 不限人群
        if (userScopeType == null || Long.valueOf(0L).equals(userScopeType)) {
            return true;
        }
        // 指定用户：查 userId 是否在白/黑名单 Set 中
        if (Long.valueOf(1L).equals(userScopeType)) {
        int participate = userParticipateType == null ? 1 : userParticipateType.intValue();
        boolean white = participate != 2;
            String mode = white ? "white" : "black";
            String key = switch (biz) {
                case PROMO -> PmCacheKeys.promoUser(tenant, channel, mode, bizId);
                case COUPON -> PmCacheKeys.couponUser(tenant, channel, mode, bizId);
                case GRANT -> PmCacheKeys.couponGrantUser(tenant, channel, mode, bizId);
            };
            boolean hit = Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userId)));
            // 白名单：命中才可用；黑名单：命中则不可用
            return white ? hit : !hit;
        }
        // 指定会员等级：仅白名单，必须命中才可用
        if (Long.valueOf(2L).equals(userScopeType)) {
            if (userLevelId == null) {
                return false;
            }
            String key = switch (biz) {
                case PROMO -> PmCacheKeys.promoLevel(tenant, channel, "white", bizId);
                case COUPON -> PmCacheKeys.couponLevel(tenant, channel, "white", bizId);
                case GRANT -> PmCacheKeys.couponGrantLevel(tenant, channel, "white", bizId);
            };
            return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userLevelId)));
        }
        return true;
    }

    private boolean inTimeWindow(Date start, Date end, Date now) {
        if (start == null || end == null) {
            return false;
        }
        if (start.after(now)) {
            return false;
        }
        return end.after(now);
    }


    /**
     * 把「按店分组」的商品，拍平成「按行排列」的 Context 列表
     */
    private List<PromoLineContext> flattenLineContexts(List<RemotePromoShopGroupBo> groups) {
        List<PromoLineContext> contexts = new ArrayList<>();
        if (CollUtil.isEmpty(groups)) {
            return contexts;
        }
        for (RemotePromoShopGroupBo group : groups) {
            if (CollUtil.isEmpty(group.getLines())) {
                continue;
            }
            for (RemotePromoLineItemBo line : group.getLines()) {
                contexts.add(new PromoLineContext(line));
            }
        }
        return contexts;
    }

    /**
     * 给每行打标：能不能参与当前选中的跨店活动分摊。
     */
    private void markCrossEligibility(List<PromoLineContext> contexts, CrossPromoCalculationResult crossResult, String tenant, String channel) {
        // 没有行、没有跨店结果、说明没有可分摊的跨店
        if (CollUtil.isEmpty(contexts) || crossResult == null || crossResult.getSelectedActivityId() == null
            || crossResult.getDiscountAmount() == null
            || crossResult.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PromoActivity crossActivity = loadPromoActivity(tenant, channel, crossResult.getSelectedActivityId());
        if (crossActivity == null) {
            return;
        }
        PromoItemScopeContext scopeCtx = buildPromoItemScopeContext(crossActivity, tenant, channel, crossActivity.getId());
        for (PromoLineContext ctx : contexts) {
            // 判断该行是否在活动圈货内，在则该行可以分摊
            if (isProductEligibleForPromo(ctx.getRawLine(), scopeCtx)) {
                ctx.setCrossEligible(true);
            }
        }
    }

    /**
     * 跨店优惠预演并写回 Context：把「名义跨店额」摊到合资格行，更新每行残值，并返回实际扣到的跨店总额。
     */
    private BigDecimal simulateAndApplyCrossDiscount(List<PromoLineContext> contexts, BigDecimal nominalCrossDiscount) {
        if (CollUtil.isEmpty(contexts)) {
            return BigDecimal.ZERO;
        }
        if (nominalCrossDiscount == null || nominalCrossDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            contexts.forEach(PromoLineContext::resetCrossDiscount);
            return BigDecimal.ZERO;
        }

        // 筛合资格行
        List<PromoLineContext> eligibleContexts = new ArrayList<>();
        for (PromoLineContext ctx : contexts) {
            if (ctx.isCrossEligible() && ctx.getCurrentRemain() != null && ctx.getCurrentRemain().compareTo(BigDecimal.ZERO) > 0) {
                eligibleContexts.add(ctx);
            }
        }

        PromoObjectAllocator.allocate(eligibleContexts, PromoLineContext::getCurrentRemain, PromoLineContext::applyCrossDiscount, nominalCrossDiscount);

        BigDecimal actualCrossTotal = BigDecimal.ZERO;
        for (PromoLineContext ctx : contexts) {
            if (ctx.getCrossDiscount() != null) {
                actualCrossTotal = actualCrossTotal.add(ctx.getCrossDiscount());
            }
        }
        return actualCrossTotal;
    }

    /**
     * 给每行打标：能不能参与「最终选中的那张券」的分摊。
     */
    private void markCouponEligibility(List<PromoLineContext> contexts, CouponMatchResult couponMatch, String tenant, String channel) {
        if (CollUtil.isEmpty(contexts) || couponMatch == null || couponMatch.getSelectedTemplateId() == null
            || couponMatch.getSelector() == null || couponMatch.getSelector().getSelectedUserCouponId() == null) {
            return;
        }
        Long templateId = couponMatch.getSelectedTemplateId();
        String json = stringRedisTemplate.opsForValue().get(PmCacheKeys.couponDetail(tenant, channel, templateId));
        if (StringUtils.isBlank(json)) {
            return;
        }
        CouponTemplate template = JsonUtils.parseObject(json, CouponTemplate.class);
        if (template == null) {
            return;
        }
        CouponItemScopeContext scopeCtx = buildCouponItemScopeContext(template, tenant, channel, templateId);
        for (PromoLineContext ctx : contexts) {
            if (isProductEligibleForCoupon(ctx.getRawLine(), scopeCtx)) {
                ctx.setCouponEligible(true);
            }
        }
    }

    /**
     * 把选中券摊到合资格行，刷回商品行，再按店汇总，并返回全单实际券优惠。
     */
    private BigDecimal allocateAndFlushToGroups(List<RemotePromoShopGroupBo> groups, List<PromoLineContext> contexts, BigDecimal totalCouponDiscount) {
        if (CollUtil.isEmpty(groups)) {
            return BigDecimal.ZERO;
        }

        if (CollUtil.isNotEmpty(contexts) && totalCouponDiscount != null && totalCouponDiscount.compareTo(BigDecimal.ZERO) > 0) {
            // 筛选要分摊的行
            List<PromoLineContext> couponEligible = new ArrayList<>();
            for (PromoLineContext ctx : contexts) {
                if (ctx.isCouponEligible() && ctx.getCurrentRemain() != null && ctx.getCurrentRemain().compareTo(BigDecimal.ZERO) > 0) {
                    couponEligible.add(ctx);
                }
            }
            PromoObjectAllocator.allocate(
                couponEligible,
                PromoLineContext::getCurrentRemain,
                PromoLineContext::applyCouponDiscount,
                totalCouponDiscount);
        }

        if (CollUtil.isNotEmpty(contexts)) {
            contexts.forEach(PromoLineContext::flushToRawLine);
        }

        BigDecimal actualCouponTotal = BigDecimal.ZERO;
        for (RemotePromoShopGroupBo group : groups) {
            // 没商品行：跨店、券 = 0，实付用店小计
            if (CollUtil.isEmpty(group.getLines())) {
                group.setShopCrossReduceDiscount(BigDecimal.ZERO);
                group.setShopCouponDiscount(BigDecimal.ZERO);
                BigDecimal shopPay = group.getShopTotalAmount() != null ? group.getShopTotalAmount() : BigDecimal.ZERO;
                group.setShopPayAmount(shopPay.max(BigDecimal.ZERO));
                        continue;
                    }
            BigDecimal shopCross = BigDecimal.ZERO;
            BigDecimal shopCoupon = BigDecimal.ZERO;
            BigDecimal shopPay = BigDecimal.ZERO;
            for (RemotePromoLineItemBo line : group.getLines()) {
                if (line.getCrossDiscountAmount() != null) {
                    shopCross = shopCross.add(line.getCrossDiscountAmount());
                }
                if (line.getCouponDiscountAmount() != null) {
                    shopCoupon = shopCoupon.add(line.getCouponDiscountAmount());
                }
                if (line.getRealPayAmount() != null) {
                    shopPay = shopPay.add(line.getRealPayAmount());
                }
            }
            group.setShopCrossReduceDiscount(shopCross);
            group.setShopCouponDiscount(shopCoupon);
            group.setShopPayAmount(shopPay);
            actualCouponTotal = actualCouponTotal.add(shopCoupon);
        }
        return actualCouponTotal;
    }

    private PromoActivity loadPromoActivity(String tenant, String channel, Long activityId) {
        if (activityId == null) {
            return null;
        }
        String actJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoDetail(tenant, channel, activityId));
        if (StringUtils.isBlank(actJson)) {
            return null;
        }
        return JsonUtils.parseObject(actJson, PromoActivity.class);
    }

    /**
     * 把活动的圈货规则从配置 + Redis 名单装成一个上下文对象，供后面判断「这行商品能不能参加该促销」。
     * @param activity 活动主数据（
     * @param tenant   租户
     * @param channel  渠道
     * @param actId    活动 ID
     * @return 圈货上下文（类型 + 供应商白名单 + 商品白/黑名单 Set）
     */
    private PromoItemScopeContext buildPromoItemScopeContext(PromoActivity activity, String tenant,
                                                             String channel, Long actId) {
        Long scopeSupplierType = activity.getScopeSupplierType() == null ? 0L : activity.getScopeSupplierType();
        Long itemParticipateType = activity.getItemParticipateType() == null ? 1L : activity.getItemParticipateType();
        Long scopeItemType = activity.getScopeItemType() == null ? 0L : activity.getScopeItemType();

        // 指定供应商(1)：从 Redis 拉供应商白名单；否则名单为空（判断时不限供应商）
        Set<String> supplierWhiteSet = Collections.emptySet();
        if (Long.valueOf(1L).equals(scopeSupplierType)) {
            Set<String> members = stringRedisTemplate.opsForSet().members(
                PmCacheKeys.promoTarget(tenant, channel, "supplier", actId));
            if (CollUtil.isNotEmpty(members)) {
                supplierWhiteSet = members;
            }
        }

        // 指定商品维度(非0) 且 白/黑名单(1/2)：按 sku/category/brand + white/black 拉目标 Set
        Set<String> itemTargetSet = Collections.emptySet();
        if (!Long.valueOf(0L).equals(scopeItemType)
            && (Long.valueOf(1L).equals(itemParticipateType) || Long.valueOf(2L).equals(itemParticipateType))) {
            String dim = null;
            if (Long.valueOf(1L).equals(scopeItemType)) {
                dim = "sku";
            } else if (Long.valueOf(2L).equals(scopeItemType)) {
                dim = "category";
            } else if (Long.valueOf(3L).equals(scopeItemType)) {
                dim = "brand";
            }
            if (dim != null) {
                String mode = Long.valueOf(1L).equals(itemParticipateType) ? "white" : "black";
                Set<String> members = stringRedisTemplate.opsForSet().members(
                    PmCacheKeys.promoTarget(tenant, channel, dim, mode, actId));
                if (CollUtil.isNotEmpty(members)) {
                    itemTargetSet = members;
                }
            }
        }
        return new PromoItemScopeContext(scopeSupplierType, itemParticipateType, scopeItemType,
            supplierWhiteSet, itemTargetSet);
    }

    /**
     * 判断这一行商品能不能参加该促销，是否命中圈定的范围
     */
    private boolean isProductEligibleForPromo(RemotePromoLineItemBo item, PromoItemScopeContext ctx) {
        // 指定供应商：不在白名单则不适用（白名单为 userinfoCode / member_ccode）
        if (Long.valueOf(1L).equals(ctx.getScopeSupplierType())
            && (StringUtils.isBlank(item.getMemberCcode())
            || !ctx.getSupplierWhiteSet().contains(item.getMemberCcode()))) {
            return false;
        }

        // ----- 商品白名单：必须命中 -----
        if (Long.valueOf(1L).equals(ctx.getItemParticipateType())) {
            // 全场通用，直接放行
            if (Long.valueOf(0L).equals(ctx.getScopeItemType())) {
                return true;
            }
            // 按 SKU 白名单
            if (Long.valueOf(1L).equals(ctx.getScopeItemType()) && item.getSkuId() != null) {
                return ctx.getItemTargetSet().contains(String.valueOf(item.getSkuId()));
            }
            // 按类目白名单（祖先路径任一命中即可，父类目活动可覆盖子类商品）
            if (Long.valueOf(2L).equals(ctx.getScopeItemType()) && CollUtil.isNotEmpty(item.getCategoryAncestorIds())) {
                for (Long catId : item.getCategoryAncestorIds()) {
                    if (catId != null && ctx.getItemTargetSet().contains(String.valueOf(catId))) {
                        return true;
                    }
                }
                return false;
            }
            // 按品牌白名单
            if (Long.valueOf(3L).equals(ctx.getScopeItemType()) && item.getBrandId() != null) {
                return ctx.getItemTargetSet().contains(String.valueOf(item.getBrandId()));
            }
            return false;
        }

        // ----- 商品黑名单：命中则排除，其余可用 -----
        if (Long.valueOf(2L).equals(ctx.getItemParticipateType())) {
            // 按 SKU 黑名单
            if (Long.valueOf(1L).equals(ctx.getScopeItemType()) && item.getSkuId() != null) {
                return !ctx.getItemTargetSet().contains(String.valueOf(item.getSkuId()));
            }
            // 按类目黑名单：祖先路径任一命中则排除
            if (Long.valueOf(2L).equals(ctx.getScopeItemType()) && CollUtil.isNotEmpty(item.getCategoryAncestorIds())) {
                for (Long catId : item.getCategoryAncestorIds()) {
                    if (catId != null && ctx.getItemTargetSet().contains(String.valueOf(catId))) {
                        return false;
                    }
                }
                return true;
            }
            // 按品牌黑名单
            if (Long.valueOf(3L).equals(ctx.getScopeItemType()) && item.getBrandId() != null) {
                return !ctx.getItemTargetSet().contains(String.valueOf(item.getBrandId()));
            }
            return true;
        }
        return false;
    }

    /**
     * 匹配用户手中未使用的优惠券，划分可用/不可用，并决定最终使用哪张。
     * @param userId            当前用户
     * @param lineContexts      行上下文
     * @param baseAmount        券抵扣封顶基数：商品合计 − 店促 − 跨店
     * @param specifiedCouponId 券三态：具体 ID=锁定（不可用阻断）；-1=不用券；null=自动最优
     * @param tenant            租户
     * @param channel           渠道
     * @param now               当前时间
     */
    private CouponMatchResult matchAndSelectBestCoupon(Long userId, List<PromoLineContext> lineContexts,
                                                       BigDecimal baseAmount, Long specifiedCouponId,
                                                       String tenant, String channel, Date now) {
        CouponMatchResult match = new CouponMatchResult();
        RemoteCouponSelectorBo selector = new RemoteCouponSelectorBo();
        List<RemoteAvailableCouponBo> availableList = new ArrayList<>();
        List<RemoteDisabledCouponBo> disabledList = new ArrayList<>();
        selector.setAvailableCoupons(availableList);
        selector.setDisabledCoupons(disabledList);
        match.setSelector(selector);

        // 无用户或扣完店促/跨店后已无可付金额 → 不选券
        if (userId == null || baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return match;
        }

        // 用户当前未使用且在有效期内的券
        List<CouponUserRel> myCoupons = couponUserRelMapper.selectUnusedCoupons(tenant, userId, now);
        if (CollUtil.isEmpty(myCoupons)) {
            return match;
        }

        // 批量取券模板详情
        List<String> tplKeys = myCoupons.stream()
            .map(c -> PmCacheKeys.couponDetail(tenant, channel, c.getCouponTemplateId()))
            .collect(Collectors.toList());
        List<String> tplJsons = stringRedisTemplate.opsForValue().multiGet(tplKeys);
        // 仅可用券进入候选池，用于自动选最优
        List<CouponCandidateInternal> candidatePool = new ArrayList<>();

        for (int i = 0; i < myCoupons.size(); i++) {
            CouponUserRel userRel = myCoupons.get(i);
            String json = (tplJsons != null && i < tplJsons.size()) ? tplJsons.get(i) : null;
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponTemplate template = JsonUtils.parseObject(json, CouponTemplate.class);
            if (template == null || template.getId() == null) {
                continue;
            }

            Long templateId = userRel.getCouponTemplateId();
            BigDecimal minSpend = template.getMinSpendAmount() != null ? template.getMinSpendAmount() : BigDecimal.ZERO;
            BigDecimal benefitValue = template.getBenefitValue() != null ? template.getBenefitValue() : BigDecimal.ZERO;
            String couponType = template.getCouponType();

            // 这张券在当前订单上的合资格金额
            BigDecimal eligibleCouponAmount = calculateEligibleAmountForCoupon(template, lineContexts, tenant, channel, templateId);

            // 无适用商品 → 不可用
            if (eligibleCouponAmount.compareTo(BigDecimal.ZERO) <= 0) {
                RemoteDisabledCouponBo dis = new RemoteDisabledCouponBo();
                dis.setUserCouponId(userRel.getId());
                dis.setTitle(template.getTitle());
                dis.setMinSpendAmount(minSpend);
                dis.setBenefitValue(benefitValue);
                dis.setDisableReason("订单中无适用商品");
                disabledList.add(dis);
                continue;
            }

            // 合计大于门槛，进可用列表
            if (eligibleCouponAmount.compareTo(minSpend) >= 0) {
                // 满门槛：满减用面额；折扣券优惠 = 合资格金额 × (1 - 折扣率)
                BigDecimal discount = COUPON_TYPE_REDUCE.equalsIgnoreCase(couponType)
                    ? benefitValue
                    : eligibleCouponAmount.multiply(BigDecimal.ONE.subtract(benefitValue))
                        .setScale(2, RoundingMode.HALF_UP);
                // 不能超过合资格金额，也不能超过扣完店促/跨店后的应付基数
                discount = discount.min(eligibleCouponAmount).min(baseAmount);

                RemoteAvailableCouponBo avail = new RemoteAvailableCouponBo();
                avail.setUserCouponId(userRel.getId());
                avail.setTemplateId(templateId);
                avail.setTitle(template.getTitle());
                avail.setCouponType(couponType);
                avail.setGrantType(template.getGrantType());
                avail.setMinSpendAmount(minSpend);
                avail.setBenefitValue(benefitValue);
                avail.setDiscountAmount(discount);
                avail.setUseEndTime(userRel.getEndTime());
                avail.setPlatformShareRate(template.getPlatformShareRate());
                avail.setMerchantShareRate(template.getMerchantShareRate());
                availableList.add(avail);
                candidatePool.add(new CouponCandidateInternal(userRel.getId(), discount, userRel.getEndTime(), minSpend));
            } else {
                // 未满门槛 → 不可用，提示还差多少
                RemoteDisabledCouponBo dis = new RemoteDisabledCouponBo();
                dis.setUserCouponId(userRel.getId());
                dis.setTitle(template.getTitle());
                dis.setMinSpendAmount(minSpend);
                dis.setBenefitValue(benefitValue);
                dis.setDisableReason("适用商品还差 ¥"
                    + minSpend.subtract(eligibleCouponAmount).setScale(2, RoundingMode.HALF_UP) + " 可用");
                disabledList.add(dis);
            }
        }

        // 从可用券候选池里自动挑一张「最优」券    优惠金额更大优先  金额相同 → 到期更早优先   仍相同 → 门槛更低优先
        Long autoBestUserCouponId = null;
        if (CollUtil.isNotEmpty(candidatePool)) {
            candidatePool.sort((c1, c2) -> {
                int cmpDiscount = c2.getDiscountAmount().compareTo(c1.getDiscountAmount());
                if (cmpDiscount != 0) {
                    return cmpDiscount;
                }
                if (c1.getEndTime() != null && c2.getEndTime() != null) {
                    int cmpTime = c1.getEndTime().compareTo(c2.getEndTime());
                    if (cmpTime != 0) {
                        return cmpTime;
                    }
                }
                return c1.getMinSpend().compareTo(c2.getMinSpend());
            });
            autoBestUserCouponId = candidatePool.get(0).getUserCouponId();
        }

        // 决定最终选中：-1 不用券 / 指定券锁定 / null 自动最优
        if (specifiedCouponId != null && specifiedCouponId == -1L) {
            selector.setSelectedUserCouponId(null);
        } else if (specifiedCouponId != null) {
            boolean specifiedAvailable = availableList.stream()
                .anyMatch(c -> c.getUserCouponId().equals(specifiedCouponId));
            if (specifiedAvailable) {
                selector.setSelectedUserCouponId(specifiedCouponId);
            } else {
                // 提交锁定的券已不可用：优先回显禁用原因，否则通用失效文案
                String reason = disabledList.stream()
                    .filter(d -> specifiedCouponId.equals(d.getUserCouponId()))
                    .map(RemoteDisabledCouponBo::getDisableReason)
                    .findFirst()
                    .orElse("优惠券已失效或不可用，请返回重新结算");
                throw new ServiceException(reason);
            }
        } else {
            selector.setSelectedUserCouponId(autoBestUserCouponId);
        }

        // 回写选中券的抵扣额与模板 ID
        if (selector.getSelectedUserCouponId() != null) {
            for (RemoteAvailableCouponBo c : availableList) {
                if (selector.getSelectedUserCouponId().equals(c.getUserCouponId())) {
                    match.setTotalCouponDiscount(c.getDiscountAmount() != null ? c.getDiscountAmount() : BigDecimal.ZERO);
                    match.setSelectedTemplateId(c.getTemplateId());
                    break;
                }
            }
        }
        return match;
    }

    /**
     * 计算某张券在当前订单上的「合资格金额」：圈货命中的商品行，把它们扣完店促（和跨店）后的剩余加起来。
     */
    private BigDecimal calculateEligibleAmountForCoupon(CouponTemplate template, List<PromoLineContext> contexts, String tenant, String channel, Long templateId) {
        if (CollUtil.isEmpty(contexts)) {
            return BigDecimal.ZERO;
        }
        CouponItemScopeContext scopeCtx = buildCouponItemScopeContext(template, tenant, channel, templateId);
        BigDecimal eligibleTotal = BigDecimal.ZERO;
        for (PromoLineContext lineCtx : contexts) {
            if (!isProductEligibleForCoupon(lineCtx.getRawLine(), scopeCtx)) {
                continue;
            }
            BigDecimal remain = lineCtx.getCurrentRemain();
            if (remain != null && remain.compareTo(BigDecimal.ZERO) > 0) {
                eligibleTotal = eligibleTotal.add(remain);
            }
        }
        return eligibleTotal;
    }

    /** 跨店未实际生效时清空选中态与名义额。 */
    private void clearCrossSelection(CrossPromoCalculationResult crossResult) {
        if (crossResult == null) {
            return;
        }
        crossResult.setDiscountAmount(BigDecimal.ZERO);
        crossResult.setSelectedActivityId(null);
        crossResult.setSelectedPromoType(null);
        crossResult.setSelectedActivityTitle(null);
        crossResult.setPlatformShareRate(null);
        crossResult.setMerchantShareRate(null);
    }

    /**
     * 把各店的跨店优惠加总成全单跨店总额。
     */
    private BigDecimal sumShopCrossReduceDiscount(List<RemotePromoShopGroupBo> groups) {
        if (CollUtil.isEmpty(groups)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (RemotePromoShopGroupBo group : groups) {
            if (group.getShopCrossReduceDiscount() != null) {
                total = total.add(group.getShopCrossReduceDiscount());
            }
        }
        return total;
    }

    /**
     * 从券模板配置 + Redis 名单，组装「某行商品是否适用该券」所需的圈货上下文。
     *
     * @param template   券模板
     * @param tenant     租户
     * @param channel    渠道
     * @param templateId 模板 ID
     */
    private CouponItemScopeContext buildCouponItemScopeContext(CouponTemplate template, String tenant,
                                                              String channel, Long templateId) {
        Long scopeItemType = template.getScopeItemType() == null ? 0L : template.getScopeItemType();
        Long itemParticipateType = template.getItemParticipateType() == null ? 1L : template.getItemParticipateType();
        Long scopeSupplierType = template.getScopeSupplierType() == null ? 0L : template.getScopeSupplierType();

        // 供应商白名单：仅当配置为「指定供应商」时从 Redis 拉取
        Set<String> supplierWhiteSet = Collections.emptySet();
        if (Long.valueOf(1L).equals(scopeSupplierType)) {
            Set<String> members = stringRedisTemplate.opsForSet().members(
                PmCacheKeys.couponTarget(tenant, channel, "supplier", templateId));
            if (CollUtil.isNotEmpty(members)) {
                supplierWhiteSet = members;
            }
        }

        // 商品目标名单：有商品维度限制时，按 SKU/类目/品牌 + 白/黑 模式拉取
        Set<String> itemTargetSet = Collections.emptySet();
        if (!Long.valueOf(0L).equals(scopeItemType)
            && (Long.valueOf(1L).equals(itemParticipateType) || Long.valueOf(2L).equals(itemParticipateType))) {
            String dim = null;
            if (Long.valueOf(1L).equals(scopeItemType)) {
                dim = "sku";
            } else if (Long.valueOf(2L).equals(scopeItemType)) {
                dim = "category";
            } else if (Long.valueOf(3L).equals(scopeItemType)) {
                dim = "brand";
            }
            if (dim != null) {
                // white=仅名单内可用；black=名单内排除
                String mode = Long.valueOf(1L).equals(itemParticipateType) ? "white" : "black";
                Set<String> members = stringRedisTemplate.opsForSet().members(
                    PmCacheKeys.couponTarget(tenant, channel, dim, mode, templateId));
                if (CollUtil.isNotEmpty(members)) {
                    itemTargetSet = members;
                }
            }
        }
        return new CouponItemScopeContext(scopeSupplierType, itemParticipateType, scopeItemType,
            supplierWhiteSet, itemTargetSet);
    }

    private boolean isProductEligibleForCoupon(RemotePromoLineItemBo item, CouponItemScopeContext ctx) {
        if (Long.valueOf(1L).equals(ctx.getScopeSupplierType())
            && (StringUtils.isBlank(item.getMemberCcode())
            || !ctx.getSupplierWhiteSet().contains(item.getMemberCcode()))) {
            return false;
        }

        if (Long.valueOf(1L).equals(ctx.getItemParticipateType())) {
            if (Long.valueOf(0L).equals(ctx.getScopeItemType())) {
                return true;
            }
            if (Long.valueOf(1L).equals(ctx.getScopeItemType()) && item.getSkuId() != null) {
                return ctx.getItemTargetSet().contains(String.valueOf(item.getSkuId()));
            }
            if (Long.valueOf(2L).equals(ctx.getScopeItemType()) && CollUtil.isNotEmpty(item.getCategoryAncestorIds())) {
                for (Long catId : item.getCategoryAncestorIds()) {
                    if (catId != null && ctx.getItemTargetSet().contains(String.valueOf(catId))) {
                        return true;
                    }
                }
                return false;
            }
            if (Long.valueOf(3L).equals(ctx.getScopeItemType()) && item.getBrandId() != null) {
                return ctx.getItemTargetSet().contains(String.valueOf(item.getBrandId()));
            }
            return false;
        }
        if (Long.valueOf(2L).equals(ctx.getItemParticipateType())) {
            if (Long.valueOf(1L).equals(ctx.getScopeItemType()) && item.getSkuId() != null) {
                return !ctx.getItemTargetSet().contains(String.valueOf(item.getSkuId()));
            }
            if (Long.valueOf(2L).equals(ctx.getScopeItemType()) && CollUtil.isNotEmpty(item.getCategoryAncestorIds())) {
                for (Long catId : item.getCategoryAncestorIds()) {
                    if (catId != null && ctx.getItemTargetSet().contains(String.valueOf(catId))) {
                        return false;
                    }
                }
                return true;
            }
            if (Long.valueOf(3L).equals(ctx.getScopeItemType()) && item.getBrandId() != null) {
                return !ctx.getItemTargetSet().contains(String.valueOf(item.getBrandId()));
            }
            return true;
        }
        return false;
    }

}
