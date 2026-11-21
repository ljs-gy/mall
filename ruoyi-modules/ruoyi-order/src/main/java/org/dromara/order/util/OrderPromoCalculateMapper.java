package org.dromara.order.util;

import cn.hutool.core.collection.CollUtil;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;
import org.dromara.order.domain.vo.PcCheckoutGiftItemVo;
import org.dromara.order.domain.vo.PcCheckoutMerchantGroupVo;
import org.dromara.order.domain.vo.PcCheckoutProductVo;
import org.dromara.pm.api.domain.RemoteExclusivePromoDeductBo;
import org.dromara.pm.api.domain.PromoCalculateModes;
import org.dromara.pm.api.domain.RemotePromoCalculateBo;
import org.dromara.pm.api.domain.RemotePromoCalculateResult;
import org.dromara.pm.api.domain.RemotePromoGiftItemBo;
import org.dromara.pm.api.domain.RemotePromoLineItemBo;
import org.dromara.pm.api.domain.RemotePromoShopGroupBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class OrderPromoCalculateMapper {

    private OrderPromoCalculateMapper() {
    }


    public static RemotePromoCalculateBo toCalculateBo(String tenantId, String channelCode,
                                                       Long userId, Long userLevelId, Long exclusiveActivityId,
                                                       Long userCouponId,
                                                       Long specifiedCrossActivityId,
                                                       List<PcCheckoutMerchantGroupVo> merchantGroups) {
        RemotePromoCalculateBo bo = new RemotePromoCalculateBo();
        bo.setTenantId(tenantId);
        bo.setChannelCode(channelCode);
        bo.setUserId(userId);
        bo.setUserLevelId(userLevelId);
        bo.setExclusiveActivityId(exclusiveActivityId);
        bo.setUserCouponId(userCouponId);
        bo.setSpecifiedCrossActivityId(specifiedCrossActivityId);
        bo.setCalculateMode(PromoCalculateModes.CHECKOUT);
        List<RemotePromoShopGroupBo> shopGroups = new ArrayList<>(merchantGroups.size());
        for (PcCheckoutMerchantGroupVo group : merchantGroups) {
            shopGroups.add(toShopGroup(group));
        }
        bo.setShopGroups(shopGroups);
        return bo;
    }


    /**
     * 组装专享扣减入参。秒杀/特价仅立即购买单 SKU。
     */
    public static RemoteExclusivePromoDeductBo toExclusiveDeductBo(String tenantId, String channelCode,
                                                                   Long activityId, Long userId,
                                                                   String parentOrderNo,
                                                                   PcCheckoutConfirmVo checkoutVO) {
        RemoteExclusivePromoDeductBo bo = new RemoteExclusivePromoDeductBo();
        bo.setTenantId(tenantId);
        bo.setChannelCode(channelCode);
        bo.setActivityId(activityId);
        bo.setUserId(userId);
        bo.setParentOrderNo(parentOrderNo);
        PcCheckoutProductVo item = firstExclusiveProduct(checkoutVO);
        if (item != null) {
            bo.setSkuId(item.getSkuId());
            bo.setBuyCount(item.getBuyCount());
        }
        return bo;
    }

    private static PcCheckoutProductVo firstExclusiveProduct(PcCheckoutConfirmVo checkoutVO) {
        if (checkoutVO == null || CollUtil.isEmpty(checkoutVO.getMerchantGroups())) {
            return null;
        }
        PcCheckoutMerchantGroupVo group = checkoutVO.getMerchantGroups().get(0);
        if (group == null || CollUtil.isEmpty(group.getProductList())) {
            return null;
        }
        return group.getProductList().get(0);
    }

    /**
     * 把 PM 算价结果，回填到结算页的店铺分组 VO 上
     */
    public static void applyPromoResult(List<PcCheckoutMerchantGroupVo> merchantGroups, RemotePromoCalculateResult result) {
        if (CollUtil.isEmpty(merchantGroups) || result == null || CollUtil.isEmpty(result.getShopGroups())) {
            return;
        }
        Map<String, RemotePromoShopGroupBo> resultByMember = new HashMap<>();
        for (RemotePromoShopGroupBo sg : result.getShopGroups()) {
            resultByMember.put(sg.getMemberCode(), sg);
        }
        for (PcCheckoutMerchantGroupVo group : merchantGroups) {
            RemotePromoShopGroupBo sg = resultByMember.get(group.getMemberCode());
            if (sg == null) {
                continue;
            }
            group.setShopTotalAmount(sg.getShopTotalAmount());
            group.setShopPromoDiscount(sg.getShopPromoDiscount());
            group.setShopCrossReduceDiscount(sg.getShopCrossReduceDiscount());
            group.setShopCouponDiscount(sg.getShopCouponDiscount());
            group.setShopPayAmount(sg.getShopPayAmount());
            group.setSelectedActivityId(sg.getSelectedActivityId());
            group.setSelectedPromoType(sg.getSelectedPromoType());
            group.setSelectedActivityTitle(sg.getSelectedActivityTitle());
            applyLineResults(group.getProductList(), sg.getLines());
        }
    }

    private static RemotePromoShopGroupBo toShopGroup(PcCheckoutMerchantGroupVo group) {
        RemotePromoShopGroupBo sg = new RemotePromoShopGroupBo();
        sg.setMemberCode(group.getMemberCode());
        sg.setMemberName(group.getMemberName());
        sg.setShopTotalAmount(group.getShopTotalAmount());
        List<RemotePromoLineItemBo> lines = new ArrayList<>(group.getProductList().size());
        for (PcCheckoutProductVo product : group.getProductList()) {
            lines.add(toLineItem(product));
        }
        sg.setLines(lines);
        return sg;
    }

    private static RemotePromoLineItemBo toLineItem(PcCheckoutProductVo item) {
        RemotePromoLineItemBo line = new RemotePromoLineItemBo();
        line.setSkuId(item.getSkuId());
        line.setSpuId(item.getSpuId());
        line.setSkuCode(item.getSkuCode());
        line.setTitle(item.getTitle());
        line.setMainPicture(item.getMainPicture());
        line.setSpecText(item.getSpecText());
        line.setOriginalPrice(item.getOriginalPrice());
        line.setActualUnitPrice(item.getActualUnitPrice());
        line.setBuyCount(item.getBuyCount());
        line.setSubTotalAmount(item.getSubTotalAmount());
        line.setCategoryAncestorIds(item.getCategoryAncestorIds());
        line.setBrandId(item.getBrandId());
        line.setMemberCcode(item.getMemberCcode());
        line.setSpecifiedShopActivityId(item.getSpecifiedShopActivityId());
        return line;
    }

    private static void applyLineResults(List<PcCheckoutProductVo> products, List<RemotePromoLineItemBo> lines) {
        if (CollUtil.isEmpty(products) || CollUtil.isEmpty(lines)) {
            return;
        }
        int size = Math.min(products.size(), lines.size());
        for (int i = 0; i < size; i++) {
            PcCheckoutProductVo item = products.get(i);
            RemotePromoLineItemBo lr = lines.get(i);
            if (item == null || lr == null) {
                continue;
            }
            if (lr.getSkuId() != null && item.getSkuId() != null
                && !lr.getSkuId().equals(item.getSkuId())) {
                continue;
            }
            if (lr.getActualUnitPrice() != null) {
                item.setActualUnitPrice(lr.getActualUnitPrice());
            }
            if (lr.getSubTotalAmount() != null) {
                item.setSubTotalAmount(lr.getSubTotalAmount());
            }
            item.setSelectedShopActivityId(lr.getSelectedShopActivityId());
            item.setSelectedPromoType(lr.getSelectedPromoType());
            item.setSelectedActivityTitle(lr.getSelectedActivityTitle());
            item.setSelectedShopPlatformShareRate(lr.getSelectedShopPlatformShareRate());
            item.setSelectedShopMerchantShareRate(lr.getSelectedShopMerchantShareRate());
            item.setGiftList(toGiftItems(lr.getGiftList()));
            item.setShopDiscountAmount(lr.getShopDiscountAmount());
            item.setCrossDiscountAmount(lr.getCrossDiscountAmount());
            item.setCouponDiscountAmount(lr.getCouponDiscountAmount());
            item.setRealPayAmount(lr.getRealPayAmount());
        }
    }

    private static List<PcCheckoutGiftItemVo> toGiftItems(List<RemotePromoGiftItemBo> gifts) {
        if (CollUtil.isEmpty(gifts)) {
            return List.of();
        }
        List<PcCheckoutGiftItemVo> list = new ArrayList<>(gifts.size());
        for (RemotePromoGiftItemBo g : gifts) {
            PcCheckoutGiftItemVo vo = new PcCheckoutGiftItemVo();
            vo.setGiftId(g.getGiftId());
            vo.setGiftSkuId(g.getGiftSkuId());
            vo.setGiftCount(g.getGiftCount());
            vo.setTierRuleId(g.getTierRuleId());
            vo.setTitle(g.getTitle());
            vo.setMainPicture(g.getMainPicture());
            vo.setSpecText(g.getSpecText());
            vo.setSkuCode(g.getSkuCode());
            vo.setSpuId(g.getSpuId());
            list.add(vo);
        }
        return list;
    }
}
