package org.dromara.user.domain.bo;

import org.dromara.user.domain.Department;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 部门业务对象 um_department
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Department.class, reverseConvertGenerate = false)
public class DepartmentBo extends BaseEntity {

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
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deptName;


}
