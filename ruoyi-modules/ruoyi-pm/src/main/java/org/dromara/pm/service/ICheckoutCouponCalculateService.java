package org.dromara.pm.service;

import org.dromara.pm.api.domain.RemoteCouponSelectorBo;
import org.dromara.pm.domain.bo.CheckoutCouponShopSnapshot;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 结算优惠券匹配与选券
 */
public interface ICheckoutCouponCalculateService {

    /**
     * 匹配用户券并选出最终使用的券。
     *
     * @param specifiedCouponId -1 不用券；非空且不可用则抛错；null 系统最优
     */
    RemoteCouponSelectorBo matchAndSelect(String tenant, String channel, Long userId,
                                          Long specifiedCouponId, BigDecimal baseAmount,
                                          Date now, List<CheckoutCouponShopSnapshot> shops);
}
