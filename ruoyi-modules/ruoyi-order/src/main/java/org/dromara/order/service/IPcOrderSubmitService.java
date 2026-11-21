package org.dromara.order.service;

import org.dromara.order.domain.bo.PcBuyNowOrderSubmitBo;
import org.dromara.order.domain.bo.PcCartOrderSubmitBo;
import org.dromara.order.domain.vo.PcOrderSubmitResultVo;

/**
 * PC 商城提交订单
 */
public interface IPcOrderSubmitService {

    /**
     * 购物车下单
     */
    PcOrderSubmitResultVo submitFromCart(PcCartOrderSubmitBo bo);

    /**
     * 立即购买
     */
    PcOrderSubmitResultVo submitBuyNow(PcBuyNowOrderSubmitBo bo);
}
