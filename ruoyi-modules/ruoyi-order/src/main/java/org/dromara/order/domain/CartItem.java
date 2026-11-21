package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 购物车明细对象 oc_cart_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_cart_item")
public class CartItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 买家ID
     */
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    private String memberBcode;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 物权编码
     */
    private String memberCode;

    /**
     * 关联商品 SPU ID
     */
    private Long spuId;

    /**
     * 关联商品 SKU ID
     */
    private Long skuId;

    /**
     * 货品编号
     */
    private String skuCode;

    /**
     * 加购数量
     */
    private Long quantity;

    /**
     * 加购时SKU单价快照
     */
    private Long addPrice;

    /**
     * 勾选状态：0未选中，1已选中
     */
    private Long isChecked;

    /**
     * 删除标志：0存在，1删除
     */
    @TableLogic
    private String delFlag;


}
