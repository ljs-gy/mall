package org.dromara.order.mapper;

import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.vo.OrderItemVo;

import java.math.BigDecimal;

/**
 * 订单明细Mapper接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface OrderItemMapper extends BaseMapperPlus<OrderItem, OrderItemVo> {

    /**
     * 增加申请中占用
     */
    int addRefunding(@Param("id") Long id,
                     @Param("qty") int qty,
                     @Param("amount") BigDecimal amount);

    /**
     * 减少申请中占用（要求申请中数量足够）
     */
    int subRefunding(@Param("id") Long id,
                     @Param("qty") int qty,
                     @Param("amount") BigDecimal amount);

    /**
     * 申请中转为已退成功
     */
    int moveRefundingToRefunded(@Param("id") Long id,
                                @Param("qty") int qty,
                                @Param("amount") BigDecimal amount);

    /**
     * 直接增加已退成功占用（如赠品系统收回）
     */
    int addRefunded(@Param("id") Long id,
                    @Param("qty") int qty,
                    @Param("amount") BigDecimal amount);
}
