package org.dromara.user.domain.bo;

import org.dromara.user.domain.User;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 用户业务对象 um_user
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = User.class, reverseConvertGenerate = false)
public class UserBo extends BaseEntity {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户唯一代码
     */
    @NotBlank(message = "用户唯一代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userCode;

    /**
     * 关联用户信息id
     */
    @NotNull(message = "关联用户信息id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 上级用户代码
     */
    private String userPcode;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userMobile;

    /**
     * 绑定手机号
     */
    private String userBindMobile;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 登录密码
     */
    @NotBlank(message = "登录密码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userPassword;

    /**
     * 支付密码
     */
    private String userPayPassword;

    /**
     * 客服电话
     */
    private String userCustomerServicePhone;

    /**
     * 是否启用：0禁用，1启用
     */
    @NotNull(message = "是否启用：0禁用，1启用不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isEnabled;


}
