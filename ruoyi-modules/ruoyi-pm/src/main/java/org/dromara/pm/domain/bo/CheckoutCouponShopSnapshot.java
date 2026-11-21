package org.dromara.pm.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 券匹配用的店铺快照（促销后金额 + 行商品属性）
 */
@Data
public class CheckoutCouponShopSnapshot {

    private String memberCode;
    private BigDecimal shopTotalAmount = BigDecimal.ZERO;
    private BigDecimal shopPromoDiscount = BigDecimal.ZERO;
    private BigDecimal shopCrossReduceDiscount = BigDecimal.ZERO;
    private List<CheckoutCouponLineSnapshot> lines = new ArrayList<>();
}
