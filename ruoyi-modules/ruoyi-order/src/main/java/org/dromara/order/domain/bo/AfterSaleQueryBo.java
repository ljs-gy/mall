package org.dromara.order.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家/运营售后列表查询
 */
@Data
public class AfterSaleQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String afterSaleNo;

    private String orderNo;

    /** 1仅退款 2退货退款 */
    private Long serviceType;

    private Long status;

    /** 商家端强制本店；运营端可选过滤 */
    private String memberCode;
}
