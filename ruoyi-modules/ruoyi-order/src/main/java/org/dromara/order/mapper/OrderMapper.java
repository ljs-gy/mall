package org.dromara.order.mapper;

import org.dromara.order.domain.Order;
import org.dromara.order.domain.vo.OrderVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

/**
 * 订单主Mapper接口
 */
public interface OrderMapper extends BaseMapperPlus<Order, OrderVo> {

    /**
     * 超时/主动关单：仅待支付子单可取消
     */
    int cancelUnpaidByOrderNo(@Param("tenantId") String tenantId,
                              @Param("orderNo") String orderNo,
                              @Param("fromStatus") Long fromStatus,
                              @Param("fromPayStatus") Long fromPayStatus,
                              @Param("toStatus") Long toStatus);

    /**
     * 子单支付成功
     */
    int markPaidByOrderNo(@Param("tenantId") String tenantId,
                          @Param("orderNo") String orderNo,
                          @Param("fromStatus") Long fromStatus,
                          @Param("fromPayStatus") Long fromPayStatus,
                          @Param("toStatus") Long toStatus,
                          @Param("payStatus") Long payStatus,
                          @Param("payTime") java.util.Date payTime,
                          @Param("payTradeNo") String payTradeNo);

    /**
     * 商家发货：订单 20 → 30
     */
    int markShippedByOrderNo(@Param("tenantId") String tenantId,
                             @Param("orderNo") String orderNo,
                             @Param("fromStatus") Long fromStatus,
                             @Param("toStatus") Long toStatus,
                             @Param("shippingTime") java.util.Date shippingTime);

    /**
     * 确认收货：订单 30 → 40
     */
    int markReceivedByOrderNo(@Param("tenantId") String tenantId,
                              @Param("orderNo") String orderNo,
                              @Param("fromStatus") Long fromStatus,
                              @Param("toStatus") Long toStatus,
                              @Param("receiveTime") java.util.Date receiveTime);
}
