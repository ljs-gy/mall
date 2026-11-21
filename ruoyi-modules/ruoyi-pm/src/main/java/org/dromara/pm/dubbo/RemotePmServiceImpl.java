package org.dromara.pm.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.pm.api.RemotePmService;
import org.dromara.pm.api.domain.RemoteCouponUserRelBo;
import org.dromara.pm.api.domain.RemoteExclusivePromoDeductBo;
import org.dromara.pm.api.domain.RemoteGiftGrantPreviewBo;
import org.dromara.pm.api.domain.RemoteGiftGrantRewardBo;
import org.dromara.pm.api.domain.RemotePromoCalculateBo;
import org.dromara.pm.api.domain.RemotePromoCalculateResult;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.mapper.PromoActivityMapper;
import org.dromara.pm.mapper.PromoGoodsSkuMapper;
import org.dromara.pm.service.ICheckoutGiftGrantPreviewService;
import org.dromara.pm.service.ICheckoutPromoCalculateService;
import org.dromara.pm.service.PromoExclusiveAssetService;
import org.dromara.pm.service.PromoUserQuotaService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营销中心远程服务实现
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DubboService
public class RemotePmServiceImpl implements RemotePmService {

    private static final String TYPE_SECKILL = "SECKILL";
    private static final String TYPE_SPECIAL = "SPECIAL_PRICE";

    private final CouponUserRelMapper couponUserRelMapper;
    private final PromoGoodsSkuMapper promoGoodsSkuMapper;
    private final PromoActivityMapper promoActivityMapper;
    private final ICheckoutPromoCalculateService checkoutPromoCalculateService;
    private final ICheckoutGiftGrantPreviewService checkoutGiftGrantPreviewService;
    private final PromoExclusiveAssetService promoExclusiveAssetService;
    private final PromoUserQuotaService promoUserQuotaService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<RemoteCouponUserRelBo> selectUnusedCoupons(String tenantId, Long userId, Date now) {
        return TenantHelper.dynamic(tenantId, () -> {
            List<CouponUserRel> list = couponUserRelMapper.selectUnusedCoupons(tenantId, userId, now);
            return list.stream().map(this::toRemoteBo).collect(Collectors.toList());
        });
    }

    @Override
    public int useCoupon(String tenantId, Long userCouponId, Long userId) {
        return TenantHelper.dynamic(tenantId, () ->
            couponUserRelMapper.useCoupon(tenantId, userCouponId, userId));
    }

    @Override
    public int rollbackCoupon(String tenantId, Long userCouponId, Long userId, Date now) {
        return TenantHelper.dynamic(tenantId, () ->
            couponUserRelMapper.rollbackCoupon(tenantId, userCouponId, userId, now));
    }

    @Override
    public int confirmPromoStock(String tenantId, Long activityId, Long skuId, int qty) {
        return TenantHelper.dynamic(tenantId, () ->
            promoGoodsSkuMapper.confirmPromoStock(tenantId, activityId, skuId, qty));
    }

    @Override
    public RemotePromoCalculateResult calculatePromoPrice(RemotePromoCalculateBo bo) {
        String tenantId = bo != null ? bo.getTenantId() : null;
        return TenantHelper.dynamic(tenantId, () -> checkoutPromoCalculateService.calculate(bo));
    }

    @Override
    public List<RemoteGiftGrantRewardBo> previewGiftGrantRewards(RemoteGiftGrantPreviewBo bo) {
        String tenantId = bo != null ? bo.getTenantId() : null;
        return TenantHelper.dynamic(tenantId, () -> checkoutGiftGrantPreviewService.preview(bo));
    }

    @Override
    public void deductExclusivePromoStock(RemoteExclusivePromoDeductBo bo) {
        String tenantId = bo != null ? bo.getTenantId() : null;
        TenantHelper.dynamic(tenantId, () -> {
            promoExclusiveAssetService.deductExclusivePromoStock(bo);
            return null;
        });
    }

    @Override
    public void restoreExclusivePromoStock(RemoteExclusivePromoDeductBo bo) {
        String tenantId = bo != null ? bo.getTenantId() : null;
        TenantHelper.dynamic(tenantId, () -> {
            promoExclusiveAssetService.restoreExclusivePromoStock(bo);
            return null;
        });
    }

    @Override
    public String getExclusivePromoType(String tenantId, String channelCode, Long activityId) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(channelCode) || activityId == null) {
            return null;
        }
        return TenantHelper.dynamic(tenantId, () -> {
            String actJson = stringRedisTemplate.opsForValue()
                .get(PmCacheKeys.promoDetail(tenantId, channelCode, activityId));
            if (StringUtils.isNotBlank(actJson)) {
                PromoActivity act = JsonUtils.parseObject(actJson, PromoActivity.class);
                String type = normalizeExclusivePromoType(act != null ? act.getPromoType() : null);
                if (type != null) {
                    return type;
                }
            }
            PromoActivity dbAct = promoActivityMapper.selectById(activityId);
            return normalizeExclusivePromoType(dbAct != null ? dbAct.getPromoType() : null);
        });
    }

    @Override
    public List<Long> reservePromoJoinQuota(String tenantId, String channelCode, Long userId,
                                            List<Long> activityIds, String parentOrderNo) {
        return TenantHelper.dynamic(tenantId, () ->
            promoUserQuotaService.reserveJoinQuota(tenantId, channelCode, userId, activityIds, parentOrderNo));
    }

    @Override
    public void rollbackPromoJoinQuota(String tenantId, String channelCode, Long userId,
                                       List<Long> activityIds, String parentOrderNo) {
        TenantHelper.dynamic(tenantId, () -> {
            promoUserQuotaService.rollbackJoinQuota(tenantId, channelCode, userId, activityIds);
            return null;
        });
    }

    @Override
    public boolean deductCrossPromoBudget(String tenantId, String channelCode, Long activityId,
                                          BigDecimal discountAmount, String parentOrderNo) {
        return TenantHelper.dynamic(tenantId, () ->
            promoExclusiveAssetService.deductCrossPromoBudget(
                tenantId, channelCode, activityId, discountAmount, parentOrderNo));
    }

    @Override
    public void restoreCrossPromoBudget(String tenantId, String channelCode, Long activityId,
                                        BigDecimal discountAmount) {
        TenantHelper.dynamic(tenantId, () -> {
            promoExclusiveAssetService.restoreCrossPromoBudget(
                tenantId, channelCode, activityId, discountAmount);
            return null;
        });
    }

    private static String normalizeExclusivePromoType(String raw) {
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

    private RemoteCouponUserRelBo toRemoteBo(CouponUserRel source) {
        RemoteCouponUserRelBo bo = new RemoteCouponUserRelBo();
        bo.setId(source.getId());
        bo.setUserId(source.getUserId());
        bo.setCouponTemplateId(source.getCouponTemplateId());
        bo.setUserCouponCode(source.getUserCouponCode());
        bo.setMinSpendAmount(source.getMinSpendAmount());
        bo.setBenefitValue(source.getBenefitValue());
        bo.setStartTime(source.getStartTime());
        bo.setEndTime(source.getEndTime());
        bo.setStatus(source.getStatus());
        return bo;
    }
}
