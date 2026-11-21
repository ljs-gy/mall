package org.dromara.order.service;

import org.dromara.order.api.domain.RemoteOrderCreateBo;
import org.dromara.order.api.domain.RemoteOrderItemCreateBo;

/**
 * 订单本地写入（子单/明细），供同模块事务内调用。
 */
public interface IOrderWriteService {

    Long createOrder(RemoteOrderCreateBo bo);

    Long createOrderItem(RemoteOrderItemCreateBo bo);
}
