package org.dromara.order.service;

import org.dromara.order.domain.bo.PcCheckoutConfirmBo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;

/**
 * PC 商城结算确认页算价
 */
public interface IPcCheckoutService {

    /**
     * 结算确认页算价
     */
    PcCheckoutConfirmVo confirmCheckout(PcCheckoutConfirmBo bo);
}
