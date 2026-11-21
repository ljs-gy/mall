package org.dromara.order.domain.bo;

import lombok.Data;
import org.dromara.pm.api.domain.RemoteExclusivePromoDeductBo;

import java.math.BigDecimal;
import java.util.List;


@Data
public class RedisReserveResult {

    /**
     * 本单已成功扣减的专享入参（秒杀/特价）
     */
    private RemoteExclusivePromoDeductBo exclusiveDeductBo;

    /**
     * 本单已扣跨店预算的活动 ID；未扣则为 null。
     */
    private Long crossActivityId;

    /**
     * 本单已扣的跨店优惠金额（元）
     */
    private BigDecimal crossDiscountAmount;

    /**
     * 本单实际占次成功的普通促销活动 ID 列表
     */
    private List<Long> promoJoinActivityIds;
}
