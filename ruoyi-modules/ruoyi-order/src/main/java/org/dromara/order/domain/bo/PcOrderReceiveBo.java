package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 买家确认收货
 */
@Data
public class PcOrderReceiveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
