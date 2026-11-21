package org.dromara.pm.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 活动合资格商品汇总指标（仅统计落入圈货范围的行）。
 * <p>
 * 用于满减/满折门槛判断与优惠计算的基数。
 * </p>
 */
@Data
public class EligibleItemMetrics {

    /** 合资格商品小计金额合计 */
    private BigDecimal amount = BigDecimal.ZERO;
    /** 合资格商品购买件数合计 */
    private int quantity = 0;
}
