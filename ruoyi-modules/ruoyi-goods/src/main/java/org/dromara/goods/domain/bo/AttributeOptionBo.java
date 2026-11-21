package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.AttributeOption;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品属性选项业务对象 goods_attribute_option
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AttributeOption.class, reverseConvertGenerate = false)
public class AttributeOptionBo extends BaseEntity {

    /**
     * 选项主键ID
     */
    @NotNull(message = "选项主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联属性ID
     */
    @NotNull(message = "关联属性ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long attributeId;

    /**
     * 属性选项名称
     */
    @NotBlank(message = "属性选项名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String attributeOptionName;


}
