package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PC 活动落地页商品列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PcPromoGoodsQueryBo extends PcPromoPageQueryBo {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    // price_asc售价升序   price_desc售价降序
    private String sortType = "default";

    public PcPromoGoodsQueryBo() {
        setPageSize(20);
    }
}
