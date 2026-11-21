package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 促销活动审核入参
 */
@Data
public class PromoActivityAuditBo {

    @NotNull(message = "活动ID不能为空")
    private Long id;

    /**
     * true-通过, false-驳回
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean passFlag;

    private String auditRemark;

}
