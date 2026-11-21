package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 售后单明细对象 oc_after_sale_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_after_sale_item")
public class AfterSaleItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明细主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联售后单号
     */
    private String afterSaleNo;

    /**
     * 关联店铺子订单号
     */
    private String orderNo;

    /**
     * 关联订单明细ID
     */
    private Long orderItemId;

    /**
     * 退款 SKU ID
     */
    private Long skuId;

    /**
     * 货品编号
     */
    private String skuCode;

    /**
     * 商品标题快照
     */
    private String goodsTitle;

    /**
     * 规格文本快照
     */
    private String specText;

    /**
     * 商品图片快照
     */
    private String coverImage;

    /**
     * 本次申请退货数量
     */
    private Integer returnQuantity;

    /**
     * 本次退款单价快照
     */
    private BigDecimal refundPrice;

    /**
     * 当前明细分摊退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 删除标志：0存在，1删除
     */
    @TableLogic
    private String delFlag;

}
