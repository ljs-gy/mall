package org.dromara.order.domain.bo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class SavedOrderItemLine {

    private Long orderItemId;
    private String orderNo;
    private String memberCode;
    private Long skuId;
    private String skuCode;
    private int quantity;
    private boolean gift;
    private Long promoActivityId;
    private String promoType;
    private BigDecimal shopDiscountAmount;
    private BigDecimal crossDiscountAmount;
    private BigDecimal couponDiscountAmount;
    private Long selectedShopActivityId;
    private String selectedPromoType;
    private String selectedActivityTitle;
    private BigDecimal selectedShopPlatformShareRate;
    private BigDecimal selectedShopMerchantShareRate;
}
