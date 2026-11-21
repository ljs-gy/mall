package org.dromara.pm.domain.bo;

import lombok.Data;
import org.dromara.pm.api.domain.RemotePromoOptionBo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 单店促销互斥算价结果（满减/满折/满赠选优后的回写）。
 */
@Data
public class ShopPromoCalculationResult {

    /** 选中活动优惠金额（满赠为 0） */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    /** 选中的店内活动 ID */
    private Long selectedActivityId;
    /** 选中活动类型 */
    private String selectedPromoType;
    /** 选中活动标题 */
    private String selectedActivityTitle;
    /** 本店可选促销选项（含 isBest 标记） */
    private List<RemotePromoOptionBo> options = Collections.emptyList();
}
