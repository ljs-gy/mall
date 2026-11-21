package org.dromara.order.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家发货单查询
 */
@Data
public class MerchantShippingQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String shippingNo;
    private String orderNo;
    /** 10待发货 20已发货 30已签收 */
    private Long shippingStatus;
    private String memberCode;
}
