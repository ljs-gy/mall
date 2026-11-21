package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

/**
 * 满赠阶梯赠品入参
 */
@Data
public class PromoGiftItemBo {

    @NotNull(message = "赠品SKU ID不能为空", groups = { AddGroup.class })
    private Long giftSkuId;

    @NotNull(message = "赠送数量不能为空", groups = { AddGroup.class })
    @Min(value = 1, message = "赠送数量至少为1", groups = { AddGroup.class })
    private Long giftCount;

    private Long sort;
}
