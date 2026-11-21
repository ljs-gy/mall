package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 群组对象 um_group
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_group")
public class Group extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属组织代码
     */
    private String orgCode;

    /**
     * 群组名称
     */
    private String groupName;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;


}
