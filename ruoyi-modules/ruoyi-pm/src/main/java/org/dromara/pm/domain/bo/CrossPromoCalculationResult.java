package org.dromara.pm.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 跨店满减/满折算价结果。
 */
@Data
public class CrossPromoCalculationResult {

    /** 跨店优惠总额 */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    /** 选中的跨店活动 ID */
    private Long selectedActivityId;
    /** 选中跨店活动类型 */
    private String selectedPromoType;
    /** 选中跨店活动标题 */
    private String selectedActivityTitle;
    /** 平台分摊比例（如 0.3000 = 30%） */
    private BigDecimal platformShareRate;
    /** 商家分摊比例 */
    private BigDecimal merchantShareRate;
}
