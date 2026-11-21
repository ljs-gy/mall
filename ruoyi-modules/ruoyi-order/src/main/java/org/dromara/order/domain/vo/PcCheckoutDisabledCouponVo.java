package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算确认页 - 不可用优惠券
 */
@Data
public class PcCheckoutDisabledCouponVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userCouponId;
    private String title;
    private BigDecimal minSpendAmount;
    private BigDecimal benefitValue;
    private String disableReason;
}
