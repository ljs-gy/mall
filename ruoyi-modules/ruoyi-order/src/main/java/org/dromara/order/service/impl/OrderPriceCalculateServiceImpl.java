package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.goods.api.domain.RemoteMasterSkuBo;
import org.dromara.order.domain.bo.PcCheckoutConfirmBo;
import org.dromara.order.domain.bo.PcCheckoutItemBo;
import org.dromara.order.util.OrderPromoCalculateMapper;
import org.dromara.pm.api.domain.RemoteAvailableCouponBo;
import org.dromara.pm.api.domain.RemoteCouponSelectorBo;
import org.dromara.pm.api.domain.RemoteDisabledCouponBo;
import org.dromara.pm.api.domain.RemotePromoCalculateBo;
import org.dromara.pm.api.domain.RemotePromoCalculateResult;
import org.dromara.order.domain.vo.PcCheckoutAddressVo;
import org.dromara.order.domain.vo.PcCheckoutAvailableCouponVo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;
import org.dromara.order.domain.vo.PcCheckoutCouponSelectorVo;
import org.dromara.order.domain.vo.PcCheckoutDisabledCouponVo;
import org.dromara.order.domain.vo.PcCheckoutMerchantGroupVo;
import org.dromara.order.domain.vo.PcCheckoutPriceSummaryVo;
import org.dromara.order.domain.vo.PcCheckoutProductVo;
import org.dromara.order.service.IOrderPriceCalculateService;
import org.dromara.pm.api.RemotePmService;
import org.dromara.user.api.RemoteUserBizService;
import org.dromara.user.api.domain.RemoteBuyerAddressBo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单算价：商品组单、店促、跨店、券分摊、运费。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderPriceCalculateServiceImpl implements IOrderPriceCalculateService {

    private static final String MEMBER_CODE_PLATFORM = "PLATFORM";
    /** SKU 上架 */
    private static final int SKU_SALE_ON = 1;
    /** SPU 审核通过 */
    private static final int AUDIT_PASSED = 2;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    @DubboReference
    private RemotePmService remotePmService;

    @Override
    public PcCheckoutConfirmVo calculate(PcCheckoutConfirmBo bo) {
        if (!LoginHelper.isLogin()) {
            throw new ServiceException("请先登录");
        }
        Long userId = LoginHelper.getUserId();
        Long userLevelId = LoginHelper.getDistLevelId();
        String tenantId = TenantHelper.getTenantId();
        String channel = bo.getChannelCode();

        PcCheckoutConfirmVo vo = new PcCheckoutConfirmVo();
        vo.setAddress(loadAddress(bo.getAddressId(), userId));

        List<PcCheckoutMerchantGroupVo> merchantGroups = buildMerchantGroups(bo.getItems(), tenantId);
        vo.setMerchantGroups(merchantGroups);

        // 汇总各店商品小计，并累计全单商品总金额
        BigDecimal totalProductAmount = BigDecimal.ZERO;
        for (PcCheckoutMerchantGroupVo group : merchantGroups) {
            BigDecimal shopTotal = group.getProductList().stream().map(PcCheckoutProductVo::getSubTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            // 本店所有商品行小计之和
            group.setShopTotalAmount(shopTotal);
            totalProductAmount = totalProductAmount.add(shopTotal);
        }

        RemotePromoCalculateBo promoBo = OrderPromoCalculateMapper.toCalculateBo(
            tenantId, channel, userId, userLevelId, bo.getActivityId(), bo.getUserCouponId(),
            bo.getSpecifiedCrossActivityId(),
            merchantGroups);
        // 调用pm算优惠
        RemotePromoCalculateResult promoResult = remotePmService.calculatePromoPrice(promoBo);
        OrderPromoCalculateMapper.applyPromoResult(merchantGroups, promoResult);

        // 入参带了 activityId：秒杀/特价专享通道，不再叠普通促销与券分摊
        if (bo.getActivityId() != null) {
            return buildDirectExclusiveResponse(vo);
        }

        BigDecimal totalShopPromoDiscount = promoResult.getTotalShopPromoDiscount() != null ? promoResult.getTotalShopPromoDiscount() : BigDecimal.ZERO;
        BigDecimal totalCrossReduceDiscount = promoResult.getTotalCrossReduceDiscount() != null ? promoResult.getTotalCrossReduceDiscount() : BigDecimal.ZERO;
        PcCheckoutCouponSelectorVo couponSelector = toCouponSelector(promoResult.getCouponSelector());
        vo.setCouponSelector(couponSelector);
        BigDecimal totalCouponDiscount = promoResult.getTotalCouponDiscount() != null ? promoResult.getTotalCouponDiscount() : BigDecimal.ZERO;
        BigDecimal totalFreightAmount = calculateFreight(vo.getAddress(), merchantGroups);
        BigDecimal totalShopPayAfterCoupon = merchantGroups.stream()
            .map(g -> g.getShopPayAmount() != null ? g.getShopPayAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalPayAmount = totalShopPayAfterCoupon.add(totalFreightAmount).max(BigDecimal.ZERO);
        PcCheckoutPriceSummaryVo priceSummary = new PcCheckoutPriceSummaryVo();
        priceSummary.setTotalProductAmount(totalProductAmount);
        priceSummary.setTotalShopPromoDiscount(totalShopPromoDiscount);
        priceSummary.setTotalCrossReduceDiscount(totalCrossReduceDiscount);
        priceSummary.setTotalCouponDiscount(totalCouponDiscount);
        priceSummary.setTotalFreightAmount(totalFreightAmount);
        priceSummary.setFinalPayAmount(finalPayAmount);
        priceSummary.setSelectedCrossActivityId(promoResult.getSelectedCrossActivityId());
        priceSummary.setSelectedCrossPromoType(promoResult.getSelectedCrossPromoType());
        priceSummary.setSelectedCrossActivityTitle(promoResult.getSelectedCrossActivityTitle());
        priceSummary.setSelectedCrossPlatformShareRate(promoResult.getSelectedCrossPlatformShareRate());
        priceSummary.setSelectedCrossMerchantShareRate(promoResult.getSelectedCrossMerchantShareRate());
        vo.setPriceSummary(priceSummary);
        return vo;
    }


    private PcCheckoutConfirmVo buildDirectExclusiveResponse(PcCheckoutConfirmVo vo) {
        BigDecimal recalculatedTotal = BigDecimal.ZERO;
        for (PcCheckoutMerchantGroupVo group : vo.getMerchantGroups()) {
            recalculatedTotal = recalculatedTotal.add(group.getShopTotalAmount());
            group.setShopPromoDiscount(BigDecimal.ZERO);
            group.setShopCrossReduceDiscount(BigDecimal.ZERO);
            group.setShopCouponDiscount(BigDecimal.ZERO);
            group.setShopFreightAmount(BigDecimal.ZERO);
            group.setShopPayAmount(group.getShopTotalAmount());
            if (CollUtil.isNotEmpty(group.getProductList())) {
                for (PcCheckoutProductVo item : group.getProductList()) {
                    item.setShopDiscountAmount(BigDecimal.ZERO);
                    item.setCrossDiscountAmount(BigDecimal.ZERO);
                    item.setCouponDiscountAmount(BigDecimal.ZERO);
                    item.setGiftList(Collections.emptyList());
                    item.setRealPayAmount(item.getSubTotalAmount() != null
                        ? item.getSubTotalAmount() : BigDecimal.ZERO);
                }
            }
        }

        PcCheckoutCouponSelectorVo couponSelector = new PcCheckoutCouponSelectorVo();
        couponSelector.setSelectedUserCouponId(null);
        couponSelector.setAvailableCoupons(Collections.emptyList());
        couponSelector.setDisabledCoupons(Collections.emptyList());
        vo.setCouponSelector(couponSelector);

        PcCheckoutPriceSummaryVo summary = new PcCheckoutPriceSummaryVo();
        summary.setTotalProductAmount(recalculatedTotal);
        summary.setTotalShopPromoDiscount(BigDecimal.ZERO);
        summary.setTotalCrossReduceDiscount(BigDecimal.ZERO);
        summary.setTotalCouponDiscount(BigDecimal.ZERO);
        summary.setTotalFreightAmount(BigDecimal.ZERO);
        summary.setFinalPayAmount(recalculatedTotal);
        vo.setPriceSummary(summary);
        vo.setGiftGrantRewards(Collections.emptyList());

        return vo;
    }


    /**
     * 将营销中心券选择结果转为结算页 VO。
     */
    private PcCheckoutCouponSelectorVo toCouponSelector(RemoteCouponSelectorBo src) {
        PcCheckoutCouponSelectorVo selector = new PcCheckoutCouponSelectorVo();
        if (src == null) {
            selector.setAvailableCoupons(Collections.emptyList());
            selector.setDisabledCoupons(Collections.emptyList());
            return selector;
        }
        selector.setSelectedUserCouponId(src.getSelectedUserCouponId());
        List<PcCheckoutAvailableCouponVo> available = new ArrayList<>();
        if (CollUtil.isNotEmpty(src.getAvailableCoupons())) {
            for (RemoteAvailableCouponBo c : src.getAvailableCoupons()) {
                if (c == null) {
                continue;
            }
                PcCheckoutAvailableCouponVo vo = new PcCheckoutAvailableCouponVo();
                vo.setUserCouponId(c.getUserCouponId());
                vo.setTemplateId(c.getTemplateId());
                vo.setTitle(c.getTitle());
                vo.setCouponType(c.getCouponType());
                vo.setGrantType(c.getGrantType());
                vo.setMinSpendAmount(c.getMinSpendAmount());
                vo.setBenefitValue(c.getBenefitValue());
                vo.setDiscountAmount(c.getDiscountAmount());
                vo.setUseEndTime(c.getUseEndTime());
                vo.setPlatformShareRate(c.getPlatformShareRate());
                vo.setMerchantShareRate(c.getMerchantShareRate());
                available.add(vo);
            }
        }
        List<PcCheckoutDisabledCouponVo> disabled = new ArrayList<>();
        if (CollUtil.isNotEmpty(src.getDisabledCoupons())) {
            for (RemoteDisabledCouponBo d : src.getDisabledCoupons()) {
                if (d == null) {
                continue;
            }
                PcCheckoutDisabledCouponVo vo = new PcCheckoutDisabledCouponVo();
                vo.setUserCouponId(d.getUserCouponId());
                vo.setTitle(d.getTitle());
                vo.setMinSpendAmount(d.getMinSpendAmount());
                vo.setBenefitValue(d.getBenefitValue());
                vo.setDisableReason(d.getDisableReason());
                disabled.add(vo);
            }
        }
        selector.setAvailableCoupons(available);
        selector.setDisabledCoupons(disabled);
        return selector;
    }

    /**
     * 主品不可售直接拦截；skuSaleStatus=1 上架，auditStatus=2 审核通过。
     */
    private void assertSkuSaleable(RemoteMasterSkuBo goods) {
        if (!isSkuSaleable(goods)) {
            String title = StringUtils.isNotBlank(goods.getTitle()) ? goods.getTitle() : String.valueOf(goods.getSkuId());
            throw new ServiceException("商品【" + title + "】未上架或未通过审核，无法购买");
        }
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

    private PcCheckoutAddressVo loadAddress(Long addressId, Long userinfoId) {
        RemoteBuyerAddressBo remote;
        try {
            if (addressId != null) {
                remote = remoteUserBizService.getBuyerAddress(addressId);
            } else {
                remote = remoteUserBizService.getDefaultBuyerAddress(userinfoId);
            }
        } catch (Exception e) {
            log.error("结算页查询收货地址失败, addressId={}, userinfoId={}", addressId, userinfoId, e);
            throw new ServiceException("收货地址查询失败，请稍后重试");
        }
        if (remote == null) {
            if (addressId != null) {
                throw new ServiceException("收货地址不存在或已删除");
            }
            return null;
        }
        PcCheckoutAddressVo vo = new PcCheckoutAddressVo();
        vo.setAddressId(remote.getAddressId());
        vo.setConsigneeName(remote.getConsigneeName());
        vo.setConsigneeMobile(remote.getConsigneeMobile());
        vo.setProvinceName(remote.getProvinceName());
        vo.setCityName(remote.getCityName());
        vo.setDistrictName(remote.getDistrictName());
        vo.setDetailAddress(remote.getDetailAddress());
        vo.setIsDefault(remote.getIsDefault() == null ? 0 : remote.getIsDefault());
        return vo;
    }

    private List<PcCheckoutMerchantGroupVo> buildMerchantGroups(List<PcCheckoutItemBo> items, String tenantId) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }

        // 1. 批量查商品
        Set<Long> skuIds = items.stream().map(PcCheckoutItemBo::getSkuId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, RemoteMasterSkuBo> goodsMap = fetchGoodsMap(tenantId, skuIds);

        // 2. 校验 SPU 一致性、可售状态及库存
        validateGoodsAndStock(items, goodsMap);

        // 3. 按商家分组构建商品列表
        Map<String, List<PcCheckoutProductVo>> merchantMap = new LinkedHashMap<>();
        for (PcCheckoutItemBo item : items) {
            RemoteMasterSkuBo goods = goodsMap.get(item.getSkuId());
            String memberCode = goods.getMemberCode();
            if (StringUtils.isBlank(memberCode)) {
                throw new ServiceException("商品【" + goods.getTitle() + "】缺少所属店铺，无法结算");
            }
            PcCheckoutProductVo productVo = buildProductVo(goods, item.getBuyCount());
            productVo.setSpecifiedShopActivityId(item.getSpecifiedShopActivityId());
            merchantMap.computeIfAbsent(memberCode, k -> new ArrayList<>()).add(productVo);
        }

        return merchantMap.entrySet().stream()
            .map(entry -> buildMerchantGroupVo(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * 批量远程获取商品 Map
     */
    private Map<Long, RemoteMasterSkuBo> fetchGoodsMap(String tenantId, Set<Long> skuIds) {
        List<String> skuIdStrs = skuIds.stream().map(String::valueOf).collect(Collectors.toList());
        List<RemoteMasterSkuBo> goodsList;
        try {
            goodsList = remoteGoodsService.selectBySkuIds(tenantId, skuIdStrs);
        } catch (Exception e) {
            log.error("结算页批量查询商品失败, tenantId: {}, skuIds: {}", tenantId, skuIdStrs, e);
            throw new ServiceException("商品信息查询失败，请稍后重试");
        }

        if (CollUtil.isEmpty(goodsList)) {
            return Collections.emptyMap();
        }
        return goodsList.stream()
            .filter(g -> g.getSkuId() != null)
            .collect(Collectors.toMap(RemoteMasterSkuBo::getSkuId, g -> g, (k1, k2) -> k1));
    }

    /**
     * 校验商品合法性、可售状态及库存
     */
    private void validateGoodsAndStock(List<PcCheckoutItemBo> items, Map<Long, RemoteMasterSkuBo> goodsMap) {
        for (PcCheckoutItemBo item : items) {
            RemoteMasterSkuBo goods = goodsMap.get(item.getSkuId());
            if (goods == null) {
                throw new ServiceException("商品不存在或已被下架");
            }
            if (item.getSpuId() != null && goods.getSpuId() != null && !item.getSpuId().equals(goods.getSpuId())) {
                throw new ServiceException("商品 SPU/SKU 不匹配");
            }
            assertSkuSaleable(goods);
            int stock = Optional.ofNullable(goods.getOriginStock()).orElse(0);
            int buyCount = item.getBuyCount() != null ? item.getBuyCount() : 0;
            if (stock < buyCount) {
                throw new ServiceException("商品【" + goods.getTitle() + "】物理库存不足，剩余：" + stock);
            }
        }
    }

    /**
     * 组装单个商品 VO
     */
    private PcCheckoutProductVo buildProductVo(RemoteMasterSkuBo goods, int buyCount) {
        BigDecimal priceSales = Optional.ofNullable(goods.getOriginPriceSales()).orElse(BigDecimal.ZERO);

            PcCheckoutProductVo p = new PcCheckoutProductVo();
            p.setSpuId(goods.getSpuId());
            p.setSkuId(goods.getSkuId());
            p.setSkuCode(StringUtils.blankToDefault(goods.getSkuCode(), ""));
            p.setTitle(goods.getTitle());
            p.setMainPicture(goods.getCoverImage());
            p.setSpecText(goods.getSpecText());
            p.setOriginalPrice(priceSales);
            p.setActualUnitPrice(priceSales);
        p.setBuyCount(buyCount);
        p.setSubTotalAmount(priceSales.multiply(BigDecimal.valueOf(buyCount)));
            p.setCategoryAncestorIds(goods.getCategoryBackendId() == null
                ? List.of() : List.of(goods.getCategoryBackendId()));
            p.setBrandId(goods.getBrandId());
            p.setMemberCcode(goods.getMemberCcode());
        return p;
    }

    /**
     * 组装商户分组 VO
     */
    private PcCheckoutMerchantGroupVo buildMerchantGroupVo(String memberCode, List<PcCheckoutProductVo> productList) {
            PcCheckoutMerchantGroupVo g = new PcCheckoutMerchantGroupVo();
        g.setMemberCode(memberCode);
        g.setMemberName(MEMBER_CODE_PLATFORM.equals(memberCode) ? "官方自营旗舰店" : "商家店铺");
        g.setProductList(productList);
            g.setShopPromoDiscount(BigDecimal.ZERO);
            g.setShopCrossReduceDiscount(BigDecimal.ZERO);
            g.setShopCouponDiscount(BigDecimal.ZERO);
        g.setShopFreightAmount(BigDecimal.ZERO);
        return g;
    }

    /**
     * 计算各店运费并回写 {@link PcCheckoutMerchantGroupVo#setShopFreightAmount}，返回全单运费合计。
     * <p>
     * 规划：按每个商品绑定的运费模板计算，再按店铺汇总；
     * 当前模板计算未接入，各店运费占位为 0。
     * </p>
     */
    private BigDecimal calculateFreight(PcCheckoutAddressVo address,
                                        List<PcCheckoutMerchantGroupVo> merchantGroups) {
        BigDecimal totalFreight = BigDecimal.ZERO;
        if (CollUtil.isEmpty(merchantGroups)) {
            return totalFreight;
        }
        for (PcCheckoutMerchantGroupVo group : merchantGroups) {
            // TODO: 按本店商品运费模板计算后汇总；当前未实现，占位 0
            BigDecimal shopFreight = BigDecimal.ZERO;
            group.setShopFreightAmount(shopFreight);
            totalFreight = totalFreight.add(shopFreight);
        }
        return totalFreight;
    }

}
