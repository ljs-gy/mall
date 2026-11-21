package org.dromara.pm.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PC 商城营销活动列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PcPromoQueryBo extends PcPromoPageQueryBo {

    private String promoType;

    /** 0全部(预热+进行中) / 1预热 / 2进行中 */
    private Integer statusType = 0;

    public PcPromoQueryBo() {
        setPageSize(12);
    }
}
