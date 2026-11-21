package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 买家填写退货快递
 */
@Data
public class PcAfterSaleReturnExpressBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "售后单号不能为空")
    private String afterSaleNo;

    @NotBlank(message = "快递公司不能为空")
    private String returnExpressCompany;

    @NotBlank(message = "快递单号不能为空")
    private String returnExpressNo;
}
