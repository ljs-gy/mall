package org.dromara.order.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
public abstract class PcOrderSubmitBaseBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 渠道编码 */
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    /** 收货地址 ID */
    private Long addressId;

    /**
     * 选中的优惠劵id
     */
    private Long userCouponId;


    @NotNull(message = "结算应付金额不能为空")
    private BigDecimal expectedPayAmount;

    /**
     * 选中的跨店活动id
     */
    private Long specifiedCrossActivityId;

    /** 下单商品清单 */
    @NotEmpty(message = "下单商品清单不能为空")
    @Valid
    private List<PcCheckoutItemBo> items;

    /** 买家备注 */
    private String buyerRemark;
}
