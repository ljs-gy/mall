package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 满赠券发放规则审核入参
 */
@Data
public class CouponGiftGrantRuleAuditBo {

    @NotNull(message = "规则ID不能为空")
    private Long id;

    /**
     * true-通过, false-驳回
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean passFlag;

    /**
     * 审核意见/驳回原因
     */
    private String auditRemark;

}
