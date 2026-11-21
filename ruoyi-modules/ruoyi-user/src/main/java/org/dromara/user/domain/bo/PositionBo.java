package org.dromara.user.domain.bo;

import org.dromara.user.domain.Position;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 职位业务对象 um_position
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Position.class, reverseConvertGenerate = false)
public class PositionBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 所属组织代码
     */
    @NotBlank(message = "所属组织代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orgCode;

    /**
     * 挂载关系类型：1挂载在组织节点，2挂载在部门，3挂载在群组
     */
    @NotNull(message = "挂载关系类型：1挂载在组织节点，2挂载在部门，3挂载在群组不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long relationType;

    /**
     * 关联的具体实体主键ID
     */
    @NotNull(message = "关联的具体实体主键ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long relationId;

    /**
     * 职位编码
     */
    @NotBlank(message = "职位编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String positionCode;

    /**
     * 职位名称
     */
    @NotBlank(message = "职位名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String positionName;


}
