package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.SpuAttributeValue;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品SPU属性值业务对象 goods_spu_attribute_value
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SpuAttributeValue.class, reverseConvertGenerate = false)
public class SpuAttributeValueBo extends BaseEntity {

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
     * 关联属性ID
     */
    @NotNull(message = "关联属性ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long attributeId;

    /**
     * 关联属性选项ID
     */
    @NotNull(message = "关联属性选项ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long attributeOptionId;


}
