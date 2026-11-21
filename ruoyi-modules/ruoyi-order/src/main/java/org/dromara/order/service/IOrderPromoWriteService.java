package org.dromara.order.service;

import org.dromara.order.domain.bo.ExclusivePromoRef;
import org.dromara.order.domain.bo.SavedOrderItemLine;
import org.dromara.order.domain.bo.SharedPromoAssetsBo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;

import java.util.List;
import java.util.Map;

/**
 * 订单营销主表/子表写入
 */
public interface IOrderPromoWriteService {


    void writeOnSubmit(String tenantId, String channelCode, String parentOrderNo,
                       Long exclusiveActivityId, String exclusivePromoType,
                       PcCheckoutConfirmVo priceVO, List<SavedOrderItemLine> lines);


    void writeReverseOnRefund(String tenantId, String parentOrderNo, String afterSaleNo, Map<Long, Integer> orderItemRefundQty);


    void writeReverseOnCancel(String tenantId, String parentOrderNo, String orderNo);


    int casReleaseSharedAssets(String tenantId, String parentOrderNo);

    SharedPromoAssetsBo loadSharedAssets(String tenantId, String parentOrderNo);


    Map<Long, ExclusivePromoRef> loadExclusivePromos(String tenantId, List<Long> orderItemIds);
}
