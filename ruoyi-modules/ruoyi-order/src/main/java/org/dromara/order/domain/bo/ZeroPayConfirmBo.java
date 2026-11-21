package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收银台 0 元支付确认（子单）
 */
@Data
public class ZeroPayConfirmBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "子订单号不能为空")
    private String orderNo;
}
