package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ExclusivePromoRef {

    private Long orderItemId;
    private Long activityId;
    private String promoType;
}
