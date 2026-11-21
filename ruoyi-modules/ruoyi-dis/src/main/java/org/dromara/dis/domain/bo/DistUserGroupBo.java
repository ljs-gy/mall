package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.DistUserGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 分销员与分组关系业务对象 dist_user_group
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistUserGroup.class, reverseConvertGenerate = false)
public class DistUserGroupBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户信息ID
     */
    @NotNull(message = "用户信息ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 分组ID
     */
    @NotNull(message = "分组ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long groupId;


}
