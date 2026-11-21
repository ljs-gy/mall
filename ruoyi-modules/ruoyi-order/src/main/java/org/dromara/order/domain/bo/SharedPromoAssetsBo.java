package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
public class SharedPromoAssetsBo {

    private Long userCouponId;
    private Long crossActivityId;
    private BigDecimal crossDiscountAmount;
    @Builder.Default
    private List<Long> promoJoinActivityIds = new ArrayList<>();
}
