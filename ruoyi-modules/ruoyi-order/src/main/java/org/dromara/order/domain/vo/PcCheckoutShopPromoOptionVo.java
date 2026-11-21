package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算确认页 - 单店互斥活动可选项
 */
@Data
public class PcCheckoutShopPromoOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long activityId;
    private String promoType;
    private String title;
    private BigDecimal discountAmount;
    /**
     * 满赠赠品明细列表
     */
    private List<PcCheckoutGiftItemVo> giftList;
    /**
     * 系统推荐最优
     */
    private Boolean isBest;
}
