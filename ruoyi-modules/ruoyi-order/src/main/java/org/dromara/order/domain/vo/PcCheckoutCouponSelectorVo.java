package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 结算确认页 - 优惠券选择器
 */
@Data
public class PcCheckoutCouponSelectorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前选中的用户优惠券 ID */
    private Long selectedUserCouponId;
    /** 本单可用优惠券列表 */
    private List<PcCheckoutAvailableCouponVo> availableCoupons;
    /** 本单不可用优惠券列表（含不可用原因） */
    private List<PcCheckoutDisabledCouponVo> disabledCoupons;
}
