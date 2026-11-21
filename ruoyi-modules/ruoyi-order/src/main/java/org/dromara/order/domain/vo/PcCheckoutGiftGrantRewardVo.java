package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算确认页 - 满赠券发放奖励预览
 */
@Data
public class PcCheckoutGiftGrantRewardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 满赠券规则 ID */
    private Long ruleId;
    /** 满赠券活动名称 */
    private String title;
    /** 发放门槛金额 */
    private BigDecimal minOrderAmount;
    /** 赠送的优惠券模板 ID */
    private Long couponTemplateId;
    /** 赠送张数 */
    private Integer giveCouponCount;
    /** 赠送优惠券名称 */
    private String couponTitle;
    /** 赠送优惠券类型（REDUCE / DISCOUNT） */
    private String couponType;
    /** 赠送优惠券面额/折扣值 */
    private BigDecimal couponBenefitValue;
    /** 是否已达到门槛：0否，1是 */
    private Integer isHit;
    /** 距门槛还差金额（已达成则为 0） */
    private BigDecimal diffAmount;
}
