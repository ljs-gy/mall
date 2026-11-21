package org.dromara.pm.service;

import org.dromara.pm.api.domain.RemotePromoCalculateBo;
import org.dromara.pm.api.domain.RemotePromoCalculateResult;

/**
 * 结算页促销算价
 */
public interface ICheckoutPromoCalculateService {


    RemotePromoCalculateResult calculate(RemotePromoCalculateBo bo);
}
