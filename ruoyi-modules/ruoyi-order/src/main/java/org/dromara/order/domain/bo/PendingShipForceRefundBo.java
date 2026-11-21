package org.dromara.order.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 待发货强制仅退款（商家退款 / 运营强制退款）。
 * items 必填，可部分退；整单退由前端勾选全部可退行。
 */
@Data
public class PendingShipForceRefundBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "子订单号不能为空")
    private String orderNo;

    /** 退款原因 */
    private String applyReason;

    @NotEmpty(message = "退款明细不能为空")
    @Valid
    private List<PcAfterSaleApplyItemBo> items;
}
