package org.dromara.order.service;

import org.dromara.order.api.domain.RemoteAfterSaleRefundConfirmBo;


public interface IAfterSaleRefundService {


    void triggerRefundAfterCommit(String tenantId, String afterSaleNo);

    void executeRefundTrigger(String tenantId, String afterSaleNo);

    boolean confirmRefunded(RemoteAfterSaleRefundConfirmBo bo);
}
