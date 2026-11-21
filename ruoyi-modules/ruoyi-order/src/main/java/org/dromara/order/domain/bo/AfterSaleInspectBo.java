package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 售后验货入参
 */
@Data
public class AfterSaleInspectBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "售后单号不能为空")
    private String afterSaleNo;

    /** true 通过 / false 不通过 */
    @NotNull(message = "验货结果不能为空")
    private Boolean pass;

    /** 验货备注；不通过时必填 */
    private String inspectRemark;
}
