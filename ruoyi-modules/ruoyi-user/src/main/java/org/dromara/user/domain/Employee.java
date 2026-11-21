package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 员工对象 um_employee
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_employee")
public class Employee extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 员工编码
     */
    private String employeeCode;

    /**
     * 关联用户编码（um_user.user_code）
     */
    private String userCode;

    /**
     * 员工所属组织编码
     */
    private String orgCode;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 公司职位编码
     */
    private String companyPositionCode;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;


}
