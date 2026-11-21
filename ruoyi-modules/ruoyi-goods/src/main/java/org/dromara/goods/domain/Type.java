package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品类型对象 goods_type
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_type")
public class Type extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 类型主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
