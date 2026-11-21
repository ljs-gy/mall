package org.dromara.order.service;

import org.dromara.order.domain.bo.AfterSaleInspectCommand;


public interface IAfterSaleInspectService {

    void inspect(AfterSaleInspectCommand command);
}
