package org.dromara.user.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户申请审核入参
 */
@Data
public class UserApplyAuditBo {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;

    /**
     * 审核结果：1通过 2驳回
     */
    @NotNull(message = "审核结果不能为空")
    private Long auditStatus;

    /**
     * 审核意见
     */
    private String auditRemark;
}
