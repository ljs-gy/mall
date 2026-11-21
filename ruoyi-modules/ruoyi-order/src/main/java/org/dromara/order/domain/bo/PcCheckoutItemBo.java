package org.dromara.order.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 结算/下单商品行入参
 */
@Data
public class PcCheckoutItemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 购物车行 ID。
     */
    private Long cartItemId;

    /** SPU ID */
    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    /** SKU ID */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /** 购买数量 */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1件")
    private Integer buyCount;

    /**
     * 本行店促选择
     */
    private Long specifiedShopActivityId;
}
