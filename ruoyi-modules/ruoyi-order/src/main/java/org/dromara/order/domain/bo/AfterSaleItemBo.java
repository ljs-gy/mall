package org.dromara.order.domain.bo;

import org.dromara.order.domain.AfterSaleItem;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 售后单明细业务对象 oc_after_sale_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AfterSaleItem.class, reverseConvertGenerate = false)
public class AfterSaleItemBo extends BaseEntity {

    @NotNull(message = "明细主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "关联售后单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String afterSaleNo;

    @NotBlank(message = "关联店铺子订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    @NotNull(message = "关联订单明细ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orderItemId;

    @NotNull(message = "退款 SKU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long skuId;

    @NotBlank(message = "货品编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String skuCode;

    private String goodsTitle;

    private String specText;

    private String coverImage;

    @NotNull(message = "本次申请退货数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer returnQuantity;

    private BigDecimal refundPrice;

    @NotNull(message = "当前明细分摊退款金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal refundAmount;


}
