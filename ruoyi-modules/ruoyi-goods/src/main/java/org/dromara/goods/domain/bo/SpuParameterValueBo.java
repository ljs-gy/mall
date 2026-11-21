package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.SpuParameterValue;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品SPU参数值业务对象 goods_spu_parameter_value
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SpuParameterValue.class, reverseConvertGenerate = false)
public class SpuParameterValueBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商品SPU ID
     */
    @NotNull(message = "商品SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /**
     * 关联参数ID
     */
    @NotNull(message = "关联参数ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long parameterId;

    /**
     * 手动填写的参数值内容
     */
    @NotBlank(message = "参数值不能为空", groups = { AddGroup.class, EditGroup.class })
    private String parameterValueText;

}
