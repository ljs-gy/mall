package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品属性选项对象 goods_attribute_option
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_attribute_option")
public class AttributeOption extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 选项主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联属性ID
     */
    private Long attributeId;

    /**
     * 属性选项名称
     */
    private String attributeOptionName;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
