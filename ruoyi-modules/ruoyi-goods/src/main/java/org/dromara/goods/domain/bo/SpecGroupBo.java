package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.SpecGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品规格组业务对象 goods_spec_group
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SpecGroup.class, reverseConvertGenerate = false)
public class SpecGroupBo extends BaseEntity {

    /**
     * 规格组主键ID
     */
    @NotNull(message = "规格组主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商品类型ID
     */
    @NotNull(message = "关联商品类型ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long typeId;

    /**
     * 规格组名称
     */
    @NotBlank(message = "规格组名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String specGroupName;


}
