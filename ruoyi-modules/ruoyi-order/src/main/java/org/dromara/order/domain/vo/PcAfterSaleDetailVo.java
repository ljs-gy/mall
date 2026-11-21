package org.dromara.order.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 售后详情（含明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PcAfterSaleDetailVo extends AfterSaleVo {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<AfterSaleItemVo> items;
}
