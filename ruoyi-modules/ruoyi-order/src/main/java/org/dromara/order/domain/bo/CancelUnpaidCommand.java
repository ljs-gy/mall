package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;

/**
 * 未支付子单取消命令
 */
@Data
@Builder
public class CancelUnpaidCommand {

    private String tenantId;
    private String orderNo;

    private UnpaidCancelOperatorType operatorType;

    private String operatorId;

    public enum UnpaidCancelOperatorType {
        BUYER,
        MERCHANT,
        OPS,
        SYSTEM_TIMEOUT
    }
}
