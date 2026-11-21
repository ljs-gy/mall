package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;

/**
 * 售后审核命令（auditBy / 商家 memberCode 由 Service 从登录态解析）
 */
@Data
@Builder
public class AfterSaleAuditCommand {

    private String tenantId;
    private String afterSaleNo;
    private Boolean pass;
    private String auditRemark;
    private Long returnAddressId;
    /** 由入口决定：商家 / 运营 */
    private AuditorType auditorType;

    public enum AuditorType {
        MERCHANT,
        OPS
    }
}
