package org.dromara.user.domain.bo;

import org.dromara.user.domain.Employee;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 员工业务对象 um_employee
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Employee.class, reverseConvertGenerate = false)
public class EmployeeBo extends BaseEntity {

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
     * 关联用户编码（um_user.user_code）
     */
    @NotBlank(message = "关联用户编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userCode;

    /**
     * 员工所属组织编码
     */
    @NotBlank(message = "员工所属组织编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orgCode;

    /**
     * 员工姓名
     */
    @NotBlank(message = "员工姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String employeeName;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String phone;

    /**
     * 公司职位编码
     */
    private String companyPositionCode;

    /**
     * 状态（0正常 1停用）
     */
    @NotBlank(message = "状态（0正常 1停用）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;


}
