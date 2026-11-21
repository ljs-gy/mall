package org.dromara.order.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.order.domain.bo.PcCheckoutConfirmBo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;
import org.dromara.order.domain.vo.PcCheckoutGiftGrantRewardVo;
import org.dromara.order.domain.vo.PcCheckoutPriceSummaryVo;
import org.dromara.order.service.IOrderPriceCalculateService;
import org.dromara.order.service.IPcCheckoutService;
import org.dromara.pm.api.RemotePmService;
import org.dromara.pm.api.domain.RemoteGiftGrantPreviewBo;
import org.dromara.pm.api.domain.RemoteGiftGrantRewardBo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PC 结算确认页：算价委托 {@link IOrderPriceCalculateService}，本类仅补充展示类数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PcCheckoutServiceImpl implements IPcCheckoutService {

    private final IOrderPriceCalculateService orderPriceCalculateService;

    @DubboReference
    private RemotePmService remotePmService;

    @Override
    public PcCheckoutConfirmVo confirmCheckout(PcCheckoutConfirmBo bo) {
        PcCheckoutConfirmVo vo = orderPriceCalculateService.calculate(bo);
        PcCheckoutPriceSummaryVo summary = vo.getPriceSummary();
        BigDecimal finalPayAmount = summary != null && summary.getFinalPayAmount() != null ? summary.getFinalPayAmount() : BigDecimal.ZERO;
        Long userId = LoginHelper.isLogin() ? LoginHelper.getUserId() : null;
        vo.setGiftGrantRewards(previewGiftGrantRewards(
            finalPayAmount,
            bo.getChannelCode(),
            userId,
            LoginHelper.getDistLevelId()));
        return vo;
    }

    /**
     * 满额送券预告
     */
    private List<PcCheckoutGiftGrantRewardVo> previewGiftGrantRewards(BigDecimal finalPayAmount, String channel, Long userId, Long userLevelId) {
        RemoteGiftGrantPreviewBo previewBo = new RemoteGiftGrantPreviewBo();
        previewBo.setTenantId(TenantHelper.getTenantId());
        previewBo.setChannelCode(channel);
        previewBo.setUserId(userId);
        previewBo.setUserLevelId(userLevelId);
        previewBo.setFinalPayAmount(finalPayAmount);
        List<RemoteGiftGrantRewardBo> remoteList;
        try {
            remoteList = remotePmService.previewGiftGrantRewards(previewBo);
        } catch (Exception e) {
            log.warn("结算页满额送券预告失败 channel={}", channel, e);
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(remoteList)) {
            return Collections.emptyList();
        }
        List<PcCheckoutGiftGrantRewardVo> rewards = new ArrayList<>(remoteList.size());
        for (RemoteGiftGrantRewardBo src : remoteList) {
            if (src == null) {
                continue;
            }
            PcCheckoutGiftGrantRewardVo reward = new PcCheckoutGiftGrantRewardVo();
            reward.setRuleId(src.getRuleId());
            reward.setTitle(src.getTitle());
            reward.setMinOrderAmount(src.getMinOrderAmount());
            reward.setGiveCouponCount(src.getGiveCouponCount());
            reward.setCouponTemplateId(src.getCouponTemplateId());
            reward.setCouponTitle(src.getCouponTitle());
            reward.setCouponType(src.getCouponType());
            reward.setCouponBenefitValue(src.getCouponBenefitValue());
            reward.setIsHit(src.getIsHit());
            reward.setDiffAmount(src.getDiffAmount());
            rewards.add(reward);
        }
        return rewards;
    }
}
