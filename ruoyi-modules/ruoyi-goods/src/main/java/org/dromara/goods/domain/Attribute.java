package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品属性对象 goods_attribute
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_attribute")
public class Attribute extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 属性主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联商品类型ID
     */
    private Long typeId;

    /**
     * 属性名称
     */
    private String attributeName;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
