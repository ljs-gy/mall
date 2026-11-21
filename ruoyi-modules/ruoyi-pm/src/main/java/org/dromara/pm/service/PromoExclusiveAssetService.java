package org.dromara.pm.service;

import org.dromara.order.api.mq.SkuRollbackItem;
import org.dromara.pm.api.domain.RemoteExclusivePromoDeductBo;

import java.math.BigDecimal;
import java.util.List;


public interface PromoExclusiveAssetService {


    /**
     * 扣专享 Redis（活动库存/限购）
     */
    void deductExclusivePromoStock(RemoteExclusivePromoDeductBo bo);


    void restoreExclusivePromoStock(RemoteExclusivePromoDeductBo bo);


    boolean deductCrossPromoBudget(String tenantId, String channelCode, Long activityId,
                                   BigDecimal discountAmount, String parentOrderNo);


    void restoreCrossPromoBudget(String tenantId, String channelCode, Long activityId, BigDecimal discountAmount);


    boolean tryMarkAssetRefunded(String tenantId, String channelCode, String parentOrderNo, String assetType);


    void clearAssetRefundedMark(String tenantId, String channelCode, String parentOrderNo, String assetType);


    void restoreExclusiveByItems(String tenantId, String channelCode, Long activityId,
                                 boolean isSeckill, Long userId, List<SkuRollbackItem> items);
}
