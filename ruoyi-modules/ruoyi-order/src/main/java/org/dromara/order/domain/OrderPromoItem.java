package org.dromara.order.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品行营销分摊流水 oc_order_promo_item（order_no 表达子单归属）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_order_promo_item")
public class OrderPromoItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String parentOrderNo;
    private String orderNo;
    private Long orderItemId;
    /** 关联主表 ID */
    private Long orderPromoId;
    /** 正向为空串 */
    private String afterSaleNo;
    private String memberCode;
    private Long skuId;
    /** 1店内 2跨店 3券 4专享 */
    private Long promoSourceType;
    /** 1正向 2逆向 */
    private Long recordType;
    private Integer quantity;
    private BigDecimal discountAmount;
    private BigDecimal platformShareAmount;
    private BigDecimal merchantShareAmount;
}
