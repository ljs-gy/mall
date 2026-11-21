package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.DistGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 分销员分组业务对象 dist_group
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistGroup.class, reverseConvertGenerate = false)
public class DistGroupBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 分组名称
     */
    @NotBlank(message = "分组名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String groupName;


}
