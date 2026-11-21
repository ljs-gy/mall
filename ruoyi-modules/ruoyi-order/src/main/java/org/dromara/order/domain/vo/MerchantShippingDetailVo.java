package org.dromara.order.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 商家发货单详情（含明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantShippingDetailVo extends ShippingOrderVo {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ShippingOrderItemVo> items;
}
