package org.dromara.order.service;

import org.dromara.order.domain.bo.PendingShipForceRefundCommand;
import org.dromara.order.domain.vo.PcAfterSaleApplyResultVo;


public interface IPendingShipForceRefundService {

    PcAfterSaleApplyResultVo forceRefund(PendingShipForceRefundCommand command);
}
