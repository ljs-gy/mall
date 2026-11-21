package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 结算确认页 - 满赠赠品明细
 */
@Data
public class PcCheckoutGiftItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 赠品记录 ID */
    private Long giftId;
    /** 赠品 SKU ID */
    private Long giftSkuId;
    /** 货品编号 */
    private String skuCode;
    /** 赠品 SPU ID */
    private Long spuId;
    /** 赠品标题 */
    private String title;
    /** 主图 */
    private String mainPicture;
    /** 规格文本 */
    private String specText;
    /** 赠送数量 */
    private Integer giftCount;
    /** 触发赠品的阶梯门槛规则ID */
    private Long tierRuleId;
}
