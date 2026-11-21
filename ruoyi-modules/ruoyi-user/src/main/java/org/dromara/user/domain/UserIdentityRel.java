package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户主体身份关系对象 um_user_identity_rel
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_user_identity_rel")
public class UserIdentityRel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户信息ID(um_userinfo.id)
     */
    private Long userinfoId;

    /**
     * 身份类型(dealer:经销商, merchant:商家, plat:平台, buy:采购商, supplier:供应商, dis:分销员)
     */
    private String identityType;

    /**
     * 身份状态(1:启用 0:禁用)
     */
    private Long status;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}
