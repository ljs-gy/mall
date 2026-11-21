package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.PromoScopeUser;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 人群关系业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PromoScopeUser.class, reverseConvertGenerate = false)
public class PromoScopeUserBo extends BaseEntity {

    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "来源主体类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sourceType;

    @NotNull(message = "来源主体ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sourceId;

    @NotNull(message = "目标ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long targetId;

}
