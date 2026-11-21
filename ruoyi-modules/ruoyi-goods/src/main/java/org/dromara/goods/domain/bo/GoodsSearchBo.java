package org.dromara.goods.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * PC 商城商品搜索入参
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
public class GoodsSearchBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 搜索关键字（标题、SPU/SKU 编码、品牌名） */
    private String keyword;

    /** 前台分类ID */
    private Long categoryFrontendId;

    /** 品牌ID */
    private Long brandId;

    /** 最低价格 */
    private BigDecimal minPrice;

    /** 最高价格 */
    private BigDecimal maxPrice;

    /**
     * 排序字段：default / sales / price / time
     */
    private String sortField = "default";

    /**
     * 排序方向：asc / desc
     */
    private String sortOrder = "desc";

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private Boolean onlyInStock;
}
