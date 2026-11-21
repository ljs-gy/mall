package org.dromara.goods.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class PublishGoodsBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Long spuId;

    @NotNull(message = "后台三级分类不能为空")
    private Long categoryBackendId;

    @NotNull(message = "前台三级分类不能为空")
    private Long categoryFrontendId;

    @NotNull(message = "品牌不能为空")
    private Long brandId;

    @NotBlank(message = "品牌名称不能为空")
    private String brandName;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotBlank(message = "业务货号不能为空")
    private String spuNo;

    @NotBlank(message = "商品归属不能为空")
    private String goodsClass;

    @NotBlank(message = "商品产地不能为空")
    private String goodsOrigin;

    @NotBlank(message = "卖家代码不能为空")
    private String memberCode;

    @NotBlank(message = "销售渠道不能为空")
    private String channelCode;

    @NotBlank(message = "商家代码不能为空")
    private String memberMcode;

    @NotBlank(message = "客户代码不能为空")
    private String memberCcode;

    private BigDecimal priceSales;

    private BigDecimal priceCost;

    private BigDecimal priceMarket;

    @NotBlank(message = "包装单位/计价单位不能为空")
    private String priceUnit;

    @NotBlank(message = "基础单位不能为空")
    private String baseUnit;

    private Long unitRatio;

    private BigDecimal singleWeight;

    private Long weight;

    private Long minOrderQuantity;

    private Long bizGoodsType;

    private Date preSaleDeliveryTime;

    private Boolean onShelfNow;

    private Date autoOffTime;

    private String detailDesc;

    private Long freightTemplateId;

    private List<ImageBo> spuImages;

    private List<ImageBo> qualityImages;

    private List<ImageBo> traceImages;

    private List<SpuParameterValueBo> paramValues;

    private List<SpuAttributeValueBo> attrValues;

    @NotEmpty(message = "SKU列表不能为空")
    @Valid
    private List<SkuBo> skus;
}
