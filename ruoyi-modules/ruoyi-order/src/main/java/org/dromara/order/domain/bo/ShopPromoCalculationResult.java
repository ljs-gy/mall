package org.dromara.order.domain.bo;

import lombok.Data;
import org.dromara.order.domain.vo.PcCheckoutShopPromoOptionVo;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 单店促销互斥算价结果
 */
@Data
public class ShopPromoCalculationResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 最终生效优惠金额 */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    /** 最终选中的活动 ID */
    private Long selectedActivityId;
    /** 本店可切换的促销选项 */
    private List<PcCheckoutShopPromoOptionVo> options = Collections.emptyList();
}
