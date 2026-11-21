package org.dromara.pm.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 结算优惠券候选（内部排序选优用：优惠额 → 到期时间 → 门槛）。
 */
@Data
@AllArgsConstructor
public class CouponCandidateInternal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户券记录 ID */
    private Long userCouponId;
    /** 该券在本单可抵扣金额 */
    private BigDecimal discountAmount;
    /** 券到期时间 */
    private Date endTime;
    /** 使用门槛金额 */
    private BigDecimal minSpend;
}
