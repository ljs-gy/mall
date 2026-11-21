package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提交订单成功响应
 */
@Data
public class PcOrderSubmitResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 结算父单号（单店时与业务单号相同；多店合并支付/券/跨店预算锚此号） */
    private String parentOrderNo;

    /**
     * 应付总金额
     */
    private BigDecimal payAmount;

    /**
     * 未支付超时自动取消时长(分钟)。
     */
    private Integer autoCancelMinutes;
}
