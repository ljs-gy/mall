package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品SPU参数值对象 goods_spu_parameter_value
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spu_parameter_value")
public class SpuParameterValue extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 关联参数ID
     */
    private Long parameterId;

    /**
     * 手动填写的参数值内容
     */
    private String parameterValueText;

}
