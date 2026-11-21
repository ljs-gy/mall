package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 申请售后结果
 */
@Data
public class PcAfterSaleApplyResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String afterSaleNo;

    private BigDecimal refundAmount;
}
