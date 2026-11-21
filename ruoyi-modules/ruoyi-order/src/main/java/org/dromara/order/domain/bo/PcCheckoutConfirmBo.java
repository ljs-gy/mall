package org.dromara.order.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 结算确认页请求入参
 */
@Data
public class PcCheckoutConfirmBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 渠道编码
     */
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    /**
     * 仅秒杀/特价专属频道
     */
    private Long activityId;

    /**
     * 指定的收货地址ID；不传则回落当前用户默认地址，无默认则 address 为空
     */
    private Long addressId;

    /**
     * 使用的用户优惠券 ID
     */
    private Long userCouponId;

    /**
     * 选中的跨店活动
     */
    private Long specifiedCrossActivityId;

    /**
     * 结算商品清单
     */
    @NotEmpty(message = "结算商品清单不能为空")
    @Valid
    private List<PcCheckoutItemBo> items;
}
