package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.PromoScopeTarget;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 营销范围业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PromoScopeTarget.class, reverseConvertGenerate = false)
public class PromoScopeTargetBo extends BaseEntity {

    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "来源主体类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sourceType;

    @NotNull(message = "来源主体ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sourceId;

    /**
     * 1供应商, 2分类, 3品牌, 4sku
     */
    @NotNull(message = "绑定目标分类不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long targetCategory;

    @NotBlank(message = "目标标识不能为空", groups = { AddGroup.class, EditGroup.class })
    private String targetId;

}
