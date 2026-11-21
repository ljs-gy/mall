package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家发货入参
 */
@Data
public class MerchantShippingShipBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "发货单号不能为空")
    private String shippingNo;

    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    @NotBlank(message = "快递单号不能为空")
    private String expressNo;
}
