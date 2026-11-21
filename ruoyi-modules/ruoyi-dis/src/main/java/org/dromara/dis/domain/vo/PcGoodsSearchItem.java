package org.dromara.dis.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * PC 渠道商品搜索列表项
 *
 * @author ljs
 * @date 2026-08-08
 */
@Data
public class PcGoodsSearchItem {

    @JsonProperty("spu_id")
    private Long spuId;

    @JsonProperty("sku_id")
    private Long skuId;

    @JsonProperty("sku_code")
    private String skuCode;

    private String title;

    @JsonProperty("cover_image")
    private String coverImage;

    @JsonProperty("price_sales")
    private BigDecimal priceSales;

    @JsonProperty("price_market")
    private BigDecimal priceMarket;

    private Integer stock;

    @JsonProperty("sales_volume")
    private Integer salesVolume;

    @JsonProperty("spec_text")
    private String specText;

    @JsonProperty("brand_name")
    private String brandName;
}
