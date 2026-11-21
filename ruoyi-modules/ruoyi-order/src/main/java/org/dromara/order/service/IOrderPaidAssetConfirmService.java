package org.dromara.order.service;

import org.dromara.order.api.mq.OrderPaidEvent;


public interface IOrderPaidAssetConfirmService {


    void confirmAssets(OrderPaidEvent event);
}
