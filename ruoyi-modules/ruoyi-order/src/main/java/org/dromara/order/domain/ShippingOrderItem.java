package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 发货单包裹明细对象 oc_shipping_order_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_shipping_order_item")
public class ShippingOrderItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明细主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联发货单号
     */
    private String shippingNo;

    /**
     * 关联店铺子订单号
     */
    private String orderNo;

    /**
     * 关联订单明细ID
     */
    private Long orderItemId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 本包裹实际发货数量
     */
    private Long shippingQuantity;

    /**
     * 删除标志：0存在，1删除
     */
    @TableLogic
    private String delFlag;

}
