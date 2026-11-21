package org.dromara.pm.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 券匹配用的商品行快照
 */
@Data
public class CheckoutCouponLineSnapshot {

    private Long skuId;
    private BigDecimal subTotalAmount;
    private List<Long> categoryAncestorIds;
    private Long brandId;
    /** 供应商编码（member_ccode） */
    private String memberCcode;
    /** 出参：是否可参与选中券分摊 */
    private boolean eligibleForCoupon;
}
