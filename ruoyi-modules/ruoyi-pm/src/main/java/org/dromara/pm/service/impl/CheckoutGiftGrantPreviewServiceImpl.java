package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.pm.api.domain.RemoteGiftGrantPreviewBo;
import org.dromara.pm.api.domain.RemoteGiftGrantRewardBo;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.CouponGiftGrantRule;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.service.ICheckoutGiftGrantPreviewService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 满额送券预告
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CheckoutGiftGrantPreviewServiceImpl implements ICheckoutGiftGrantPreviewService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 按当前应付金额，列出用户可能拿到的赠券规则，并标出是否已达标、还差多少。规则召回和人群校验与支付后真实发券一致，但这里只预告、不发券。
     */
    @Override
    public List<RemoteGiftGrantRewardBo> preview(RemoteGiftGrantPreviewBo bo) {
        List<RemoteGiftGrantRewardBo> rewards = new ArrayList<>();
        if (bo == null || StringUtils.isBlank(bo.getTenantId()) || StringUtils.isBlank(bo.getChannelCode())) {
            return rewards;
        }
        String tenant = bo.getTenantId();
        String channel = bo.getChannelCode();
        BigDecimal finalPayAmount = bo.getFinalPayAmount() != null ? bo.getFinalPayAmount() : BigDecimal.ZERO;
        Long userId = bo.getUserId();
        Long userLevelId = bo.getUserLevelId();
        Date now = new Date();
        // 召回进行中的赠券规则
        Set<String> grantIds = stringRedisTemplate.opsForSet().members(PmCacheKeys.couponGrantActiveIds(tenant, channel));
        if (CollUtil.isEmpty(grantIds)) {
            return rewards;
        }

        List<String> idList = new ArrayList<>(grantIds);
        List<String> grantKeys = idList.stream()
            .map(id -> PmCacheKeys.couponGrantDetail(tenant, channel, Long.valueOf(id)))
            .toList();
        List<String> grantJsons = stringRedisTemplate.opsForValue().multiGet(grantKeys);
        for (String json : grantJsons) {
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponGiftGrantRule rule = JsonUtils.parseObject(json, CouponGiftGrantRule.class);
            if (rule == null || rule.getId() == null) {
                continue;
            }
            if (!tenant.equals(rule.getTenantId())) {
                continue;
            }
            if (!inTimeWindow(rule.getStartTime(), rule.getEndTime(), now)) {
                continue;
            }
            if (!checkUserEligibility(rule.getUserScopeType(), rule.getUserParticipateType(),
                userId, userLevelId, rule.getId(), tenant, channel)) {
                continue;
            }
            BigDecimal minOrderAmount = rule.getMinOrderAmount();
            if (minOrderAmount == null) {
                continue;
            }
            RemoteGiftGrantRewardBo reward = new RemoteGiftGrantRewardBo();
            reward.setRuleId(rule.getId());
            reward.setTitle(rule.getTitle());
            reward.setMinOrderAmount(minOrderAmount);
            reward.setGiveCouponCount(rule.getGiveCouponCount() == null ? 1 : rule.getGiveCouponCount().intValue());
            Long couponTemplateId = rule.getCouponTemplateId();
            reward.setCouponTemplateId(couponTemplateId);
            if (couponTemplateId != null) {
                String tplJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.couponDetail(tenant, channel, couponTemplateId));
                if (StringUtils.isNotBlank(tplJson)) {
                    CouponTemplate tpl = JsonUtils.parseObject(tplJson, CouponTemplate.class);
                    if (tpl != null) {
                        reward.setCouponTitle(tpl.getTitle());
                        reward.setCouponType(tpl.getCouponType());
                        reward.setCouponBenefitValue(tpl.getBenefitValue());
                    }
                }
            }
            if (finalPayAmount.compareTo(minOrderAmount) >= 0) {
                reward.setIsHit(1);
                reward.setDiffAmount(BigDecimal.ZERO);
            } else {
                reward.setIsHit(0);
                reward.setDiffAmount(minOrderAmount.subtract(finalPayAmount).setScale(2, RoundingMode.HALF_UP));
            }
            rewards.add(reward);
        }
        return rewards;
    }

    private boolean checkUserEligibility(Long userScopeType, Long userParticipateType,
                                         Long userId, Long userLevelId, Long ruleId,
                                         String tenant, String channel) {
        if (userScopeType == null || Long.valueOf(0L).equals(userScopeType)) {
            return true;
        }
        int participate = userParticipateType == null ? 1 : userParticipateType.intValue();
        boolean white = participate != 2;

        if (Long.valueOf(1L).equals(userScopeType)) {
            if (userId == null) {
                return !white;
            }
            String key = PmCacheKeys.couponGrantUser(tenant, channel, white ? "white" : "black", ruleId);
            boolean hit = Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userId)));
            return white ? hit : !hit;
        }
        if (Long.valueOf(2L).equals(userScopeType)) {
            if (userLevelId == null) {
                return !white;
            }
            String key = PmCacheKeys.couponGrantLevel(tenant, channel, white ? "white" : "black", ruleId);
            boolean hit = Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userLevelId)));
            return white ? hit : !hit;
        }
        return true;
    }

    /** 半开区间 [start, end)：结束瞬间不算有效 */
    private boolean inTimeWindow(Date start, Date end, Date now) {
        if (start == null || end == null) {
            return false;
        }
        return !start.after(now) && end.after(now);
    }
}
