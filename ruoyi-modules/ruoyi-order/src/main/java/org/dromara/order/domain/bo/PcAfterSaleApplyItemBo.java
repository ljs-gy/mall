package org.dromara.order.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 申请售后商品行
 */
@Data
public class PcAfterSaleApplyItemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "订单明细ID不能为空")
    private Long orderItemId;

    @NotNull(message = "退货数量不能为空")
    @Min(value = 1, message = "退货数量至少为1")
    private Integer returnQuantity;
}
