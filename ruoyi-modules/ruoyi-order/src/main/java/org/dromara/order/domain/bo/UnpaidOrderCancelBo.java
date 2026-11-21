package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 买家/运营取消未支付子单
 */
@Data
public class UnpaidOrderCancelBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "子订单号不能为空")
    private String orderNo;

    /** 取消原因备注（可选） */
    private String remark;
}
