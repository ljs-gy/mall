package org.dromara.pm.domain.bo;

import lombok.Data;
import org.dromara.pm.api.domain.RemoteCouponSelectorBo;

import java.math.BigDecimal;

/**
 * 结算选券结果：选中券抵扣额 + 券选择器 + 选中模板 ID。
 */
@Data
public class CouponMatchResult {

    /** 选中券抵扣金额*/
    private BigDecimal totalCouponDiscount = BigDecimal.ZERO;
    /** 可用/不可用券列表及选中用户券 ID */
    private RemoteCouponSelectorBo selector = new RemoteCouponSelectorBo();
    /** 选中券对应的模板 ID */
    private Long selectedTemplateId;
}
