package org.dromara.order.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 售后审核入参
 */
@Data
public class AfterSaleAuditBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "售后单号不能为空")
    private String afterSaleNo;

    /** true 通过 / false 拒绝 */
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    /** 审核留言；拒绝建议填写 */
    private String auditRemark;

    /**
     * 退货退款审核通过时必填：商家退货地址库 ID
     */
    private Long returnAddressId;
}
