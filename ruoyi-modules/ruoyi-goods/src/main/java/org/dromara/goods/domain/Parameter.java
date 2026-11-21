package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品参数对象 goods_parameter
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_parameter")
public class Parameter extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联商品类型ID
     */
    private Long typeId;

    /**
     * 参数名称
     */
    private String parameterName;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
