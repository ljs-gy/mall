package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户对象 um_user
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_user")
public class User extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户唯一代码
     */
    private String userCode;

    /**
     * 关联用户信息id
     */
    private Long userinfoId;

    /**
     * 上级用户代码
     */
    private String userPcode;

    /**
     * 手机号
     */
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
    private Long isEnabled;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;


}
