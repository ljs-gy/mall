package org.dromara.user.domain.bo;

import org.dromara.user.domain.UserIdentityRel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 用户主体身份关系业务对象 um_user_identity_rel
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserIdentityRel.class, reverseConvertGenerate = false)
public class UserIdentityRelBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户信息ID(um_userinfo.id)
     */
    @NotNull(message = "用户信息ID(um_userinfo.id)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 身份类型(dealer:经销商, merchant:商家, plat:平台, buy:采购商, supplier:供应商, dis:分销员)
     */
    @NotBlank(message = "身份类型(dealer:经销商, merchant:商家, plat:平台, buy:采购商, supplier:供应商, dis:分销员)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String identityType;

    /**
     * 身份状态(1:启用 0:禁用)
     */
    @NotNull(message = "身份状态(1:启用 0:禁用)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;


}
