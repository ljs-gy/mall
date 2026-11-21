package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * C 端「我的优惠券」查询
 * <p>
 * statusType：0未使用 1已使用 2已过期 3已作废
 */
@Data
public class PcMyCouponQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    /**
     * 0未使用 1已使用 2已过期 3已作废
     */
    @NotNull(message = "状态类型不能为空")
    @Min(value = 0, message = "状态类型不合法")
    @Max(value = 3, message = "状态类型不合法")
    private Integer statusType;

    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数必须大于0")
    private Integer pageSize = 10;
}
