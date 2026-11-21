package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 待发货强制仅退款命令（items 必填）
 */
@Data
@Builder
public class PendingShipForceRefundCommand {

    private String tenantId;
    private String orderNo;
    private String applyReason;
    private List<PcAfterSaleApplyItemBo> items;
    private OperatorType operatorType;
    private String memberCode;

    public enum OperatorType {
        MERCHANT,
        OPS
    }
}
