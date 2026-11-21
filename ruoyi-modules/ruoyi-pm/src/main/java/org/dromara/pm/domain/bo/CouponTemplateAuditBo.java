package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 优惠券模板审核入参
 */
@Data
public class CouponTemplateAuditBo {

    @NotNull(message = "优惠券模板ID不能为空")
    private Long templateId;

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
