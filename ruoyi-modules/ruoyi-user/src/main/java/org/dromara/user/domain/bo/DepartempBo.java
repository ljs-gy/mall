package org.dromara.user.domain.bo;

import org.dromara.user.domain.Departemp;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 部门员工职位关系业务对象 um_departemp
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Departemp.class, reverseConvertGenerate = false)
public class DepartempBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 员工编码
     */
    @NotBlank(message = "员工编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String employeeCode;

    /**
     * 部门ID
     */
    @NotNull(message = "部门ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long deptId;

    /**
     * 在该部门担任的职位编码
     */
    @NotBlank(message = "在该部门担任的职位编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deptPositionCode;


}
