package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Attribute;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品属性业务对象 goods_attribute
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Attribute.class, reverseConvertGenerate = false)
public class AttributeBo extends BaseEntity {

    /**
     * 属性主键ID
     */
    @NotNull(message = "属性主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商品类型ID
     */
    @NotNull(message = "关联商品类型ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long typeId;

    /**
     * 属性名称
     */
    @NotBlank(message = "属性名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String attributeName;


}
