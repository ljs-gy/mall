package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;

/**
 * 售后验货命令
 */
@Data
@Builder
public class AfterSaleInspectCommand {

    private String tenantId;
    private String afterSaleNo;
    private Boolean pass;
    private String inspectRemark;

    private InspectorType inspectorType;
    /** 商家验货时必填，用于校验本店 */
    private String memberCode;

    public enum InspectorType {
        MERCHANT,
        OPS
    }
}
