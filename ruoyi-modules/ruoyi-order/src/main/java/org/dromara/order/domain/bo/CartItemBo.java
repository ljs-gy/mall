package org.dromara.order.domain.bo;

import org.dromara.order.domain.CartItem;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 购物车明细业务对象 oc_cart_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CartItem.class, reverseConvertGenerate = false)
public class CartItemBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 买家ID
     */
    @NotNull(message = "买家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    private String memberBcode;

    /**
     * 渠道编码
     */
    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /**
     * 物权编码
     */
    @NotBlank(message = "物权编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    /**
     * 关联商品 SPU ID
     */
    @NotNull(message = "关联商品 SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /**
     * 关联商品 SKU ID
     */
    @NotNull(message = "关联商品 SKU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long skuId;

    /**
     * 货品编号
     */
    @NotBlank(message = "货品编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String skuCode;

    /**
     * 加购数量
     */
    @NotNull(message = "加购数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long quantity;

    /**
     * 加购时SKU单价快照
     */
    @NotNull(message = "加购时SKU单价快照不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long addPrice;

    /**
     * 勾选状态：0未选中，1已选中
     */
    @NotNull(message = "勾选状态：0未选中，1已选中不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isChecked;


}
