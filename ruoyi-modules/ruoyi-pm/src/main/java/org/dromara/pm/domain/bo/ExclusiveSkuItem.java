package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.math.BigDecimal;

/**
 * 秒杀 / 特价 活动商品入参
 */
@Data
public class ExclusiveSkuItem {

    @NotNull(message = "SPU ID不能为空", groups = { AddGroup.class })
    private Long spuId;

    @NotNull(message = "SKU ID不能为空", groups = { AddGroup.class })
    private Long skuId;

    @NotNull(message = "活动价不能为空", groups = { AddGroup.class })
    @DecimalMin(value = "0.01", message = "活动价必须大于0", groups = { AddGroup.class })
    private BigDecimal promoPrice;

    /**
     * 活动配额库存
     */
    @NotNull(message = "活动库存不能为空", groups = { AddGroup.class })
    @Min(value = 1, message = "活动库存必须大于0", groups = { AddGroup.class })
    private Long activityStock;

    /**
     * 每用户限购件数（-1 表示不限）
     */
    @NotNull(message = "限购设置不能为空", groups = { AddGroup.class })
    private Long userLimitQty;

}
