package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品SKU规格明细对象 goods_sku_spec
 *
 * @author ljs
 * @date 2026-11-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_sku_spec")
public class SkuSpec extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 关联 SPU ID */
    private Long spuId;

    /** 关联 SKU ID */
    private Long skuId;

    /** 规格ID（如颜色） */
    private Long specId;

    /** 规格选项ID（如红色） */
    private Long specOptionId;

    @TableLogic
    private String delFlag;

}
