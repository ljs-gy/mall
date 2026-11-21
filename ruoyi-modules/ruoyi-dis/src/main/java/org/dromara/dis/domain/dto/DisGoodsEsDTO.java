package org.dromara.dis.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 渠道商品 ES 文档（索引 dis_goods）
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
public class DisGoodsEsDTO {

    @JsonProperty("doc_id")
    private String docId;

    @JsonProperty("channel_code")
    private String channelCode;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("sku_id")
    private Long skuId;

    @JsonProperty("spu_id")
    private Long spuId;

    @JsonProperty("sku_code")
    private String skuCode;

    @JsonProperty("spu_code")
    private String spuCode;

    @JsonProperty("price_sales")
    private BigDecimal priceSales;

    @JsonProperty("price_market")
    private BigDecimal priceMarket;

    private Integer stock;

    @JsonProperty("sku_sale_status")
    private Integer skuSaleStatus;

    private String title;

    /** completion 建议词（写入 title 即可由 IK 分词） */
    private List<String> suggest;

    @JsonProperty("spec_text")
    private String specText;

    @JsonProperty("cover_image")
    private String coverImage;

    @JsonProperty("sales_volume")
    private Integer salesVolume;

    /** 排序权重（integer） */
    private Integer weight;

    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    /** putStatus：1已上架 0系统下架 2人工下架 → 映射字段 sale_status */
    @JsonProperty("sale_status")
    private Integer saleStatus;

    @JsonProperty("audit_status")
    private Integer auditStatus;

    private List<DisGoodsEsAttrItem> attrs;

    @JsonProperty("del_flag")
    private Byte delFlag;
}
