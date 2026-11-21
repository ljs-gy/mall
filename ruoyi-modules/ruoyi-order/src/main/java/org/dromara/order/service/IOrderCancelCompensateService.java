package org.dromara.order.service;

public interface IOrderCancelCompensateService {

    boolean isCancelCompensated(String tenantId, String orderNo);

    void markCancelCompensated(String tenantId, String orderNo);

    /**
     * 同父单下是否全部子单已取消
     */
    boolean allSiblingsCancelled(String tenantId, String parentOrderNo);
}
