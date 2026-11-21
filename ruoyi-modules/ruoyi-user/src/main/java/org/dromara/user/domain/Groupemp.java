package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 群组员工职位关系对象 um_groupemp
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_groupemp")
public class Groupemp extends TenantEntity {

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
     * 群组ID
     */
    private Long groupId;

    /**
     * 在该群组里担任的职位编码
     */
    private String groupPositionCode;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;


}
