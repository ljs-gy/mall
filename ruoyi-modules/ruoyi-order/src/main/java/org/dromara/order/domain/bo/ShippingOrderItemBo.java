package org.dromara.order.domain.bo;

import org.dromara.order.domain.ShippingOrderItem;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 发货单包裹明细业务对象 oc_shipping_order_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ShippingOrderItem.class, reverseConvertGenerate = false)
public class ShippingOrderItemBo extends BaseEntity {

    /**
     * 明细主键ID
     */
    @NotNull(message = "明细主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联发货单号
     */
    @NotBlank(message = "关联发货单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String shippingNo;

    /**
     * 关联店铺子订单号
     */
    @NotBlank(message = "关联店铺子订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 关联订单明细ID
     */
    @NotNull(message = "关联订单明细ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orderItemId;

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long skuId;

    /**
     * 本包裹实际发货数量
     */
    @NotNull(message = "本包裹实际发货数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long shippingQuantity;


}
