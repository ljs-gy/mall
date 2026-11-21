package org.dromara.pm.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class PcPromoGoodsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Long spuId;
    private String skuCode;
    private String spuCode;
    private String title;
    private String specText;
    private String coverImage;
    private BigDecimal priceSales;
    private BigDecimal priceMarket;
    private Integer stock;
    private Integer salesVolume;
    private Integer weight;
    private Long categoryBackendId;
    private Long categoryFrontendId;
    private Long brandId;
    private String brandName;
    private String goodsClass;
    private String memberCode;
    /** 供应商编码（member_ccode） */
    private String memberCcode;
    private Long userLimitQty;
}
