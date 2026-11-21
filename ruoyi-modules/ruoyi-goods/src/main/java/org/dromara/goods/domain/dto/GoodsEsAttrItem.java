package org.dromara.goods.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品 ES 文档中的销售属性项（nested attrs）
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
public class GoodsEsAttrItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("attr_id")
    private Long attrId;

    @JsonProperty("option_id")
    private Long optionId;

    public GoodsEsAttrItem() {
    }

    public GoodsEsAttrItem(Long attrId, Long optionId) {
        this.attrId = attrId;
        this.optionId = optionId;
    }
}
