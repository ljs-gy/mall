package org.dromara.order.service;

import org.dromara.order.domain.bo.PcCheckoutConfirmBo;
import org.dromara.order.domain.vo.PcCheckoutConfirmVo;


public interface IOrderPriceCalculateService {


    PcCheckoutConfirmVo calculate(PcCheckoutConfirmBo bo);
}
