package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 职位对象 um_position
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_position")
public class Position extends TenantEntity {

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
     * 挂载关系类型：1挂载在组织节点，2挂载在部门，3挂载在群组
     */
    private Long relationType;

    /**
     * 关联的具体实体主键ID
     */
    private Long relationId;

    /**
     * 职位编码
     */
    private String positionCode;

    /**
     * 职位名称
     */
    private String positionName;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;


}
