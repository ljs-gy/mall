package org.dromara.pm.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PC 秒杀专区商品列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PcSeckillZoneQueryBo extends PcPromoPageQueryBo {

    /** price_asc / price_desc / default */
    private String sortType = "default";
}
