package org.dromara.goods.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoodsEsIndexDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("sku_id")
    private Long skuId;

    @JsonProperty("spu_id")
    private Long spuId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("sku_code")
    private String skuCode;

    @JsonProperty("spu_code")
    private String spuCode;

    private String title;

    @JsonProperty("spec_text")
    private String specText;

    @JsonProperty("cover_image")
    private String coverImage;

    @JsonProperty("price_sales")
    private BigDecimal priceSales;

    @JsonProperty("price_market")
    private BigDecimal priceMarket;

    private Long stock;

    @JsonProperty("sales_volume")
    private Long salesVolume;

    private Long weight;

    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonProperty("category_backend_id")
    private Long categoryBackendId;

    @JsonProperty("category_frontend_id")
    private Long categoryFrontendId;

    @JsonProperty("brand_id")
    private Long brandId;

    @JsonProperty("brand_name")
    private String brandName;

    @JsonProperty("goods_class")
    private String goodsClass;

    @JsonProperty("member_code")
    private String memberCode;


    @JsonProperty("member_ccode")
    private String memberCcode;

    private List<GoodsEsAttrItem> attrs;

    @JsonProperty("sale_status")
    private Long saleStatus;

    @JsonProperty("sku_sale_status")
    private Long skuSaleStatus;

    @JsonProperty("audit_status")
    private Long auditStatus;

    @JsonProperty("del_flag")
    private String delFlag;


    @JsonProperty("channel_code")
    private String channelCode;


    @JsonProperty("promo_type")
    private String promoType;


    @JsonProperty("promo_activity_id")
    private Long promoActivityId;

    @JsonProperty("promo_price")
    private BigDecimal promoPrice;


    @JsonProperty("promo_status")
    private Integer promoStatus;


    @JsonProperty("promo_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date promoStartTime;


    @JsonProperty("promo_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date promoEndTime;
}
