package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class PcCouponClaimBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;


    @NotNull(message = "优惠券模板ID不能为空")
    private Long templateId;
}
