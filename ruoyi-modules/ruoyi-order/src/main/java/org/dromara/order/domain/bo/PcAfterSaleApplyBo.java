package org.dromara.order.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 买家申请售后
 */
@Data
public class PcAfterSaleApplyBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "子订单号不能为空")
    private String orderNo;

    /**
     * 1仅退款 2退货退款
     */
    @NotNull(message = "售后类型不能为空")
    private Long serviceType;

    @NotBlank(message = "售后理由不能为空")
    private String applyReason;

    private String applyRemark;

    private String proofImgs;

    @NotEmpty(message = "售后商品不能为空")
    @Valid
    private List<PcAfterSaleApplyItemBo> items;
}
