package org.dromara.dis.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 渠道商品 ES 属性项
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
public class DisGoodsEsAttrItem {

    @JsonProperty("attr_id")
    private Long attrId;

    @JsonProperty("attr_name")
    private String attrName;

    @JsonProperty("option_id")
    private Long optionId;

    @JsonProperty("option_value")
    private String optionValue;
}
