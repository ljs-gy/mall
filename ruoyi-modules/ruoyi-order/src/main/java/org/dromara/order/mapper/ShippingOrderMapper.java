package org.dromara.order.mapper;

import org.dromara.order.domain.ShippingOrder;
import org.dromara.order.domain.vo.ShippingOrderVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 发货单Mapper接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface ShippingOrderMapper extends BaseMapperPlus<ShippingOrder, ShippingOrderVo> {

    /**
     * 商家发货：发货单 10 → 20
     */
    int shipByStatusCas(@Param("tenantId") String tenantId,
                        @Param("shippingNo") String shippingNo,
                        @Param("fromStatus") Long fromStatus,
                        @Param("toStatus") Long toStatus,
                        @Param("expressCompany") String expressCompany,
                        @Param("expressNo") String expressNo,
                        @Param("shippingTime") Date shippingTime);

    /**
     * 确认收货：发货单 20 → 30（按订单号）
     */
    int receiveByOrderNoCas(@Param("tenantId") String tenantId,
                            @Param("orderNo") String orderNo,
                            @Param("fromStatus") Long fromStatus,
                            @Param("toStatus") Long toStatus,
                            @Param("receiveTime") Date receiveTime);
}
