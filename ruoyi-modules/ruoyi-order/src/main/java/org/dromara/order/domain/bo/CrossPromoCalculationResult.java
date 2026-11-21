package org.dromara.order.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 跨店促销互斥算价结果
 */
@Data
public class CrossPromoCalculationResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 跨店优惠总额 */
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** 最终选中的跨店活动 ID */
    private Long selectedActivityId;

    /** 最终选中的跨店活动类型 */
    private String selectedPromoType;
}
