package org.dromara.user.domain.vo;

import org.dromara.user.domain.User;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 用户视图对象 um_user
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = User.class)
public class UserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 用户唯一代码
     */
    @ExcelProperty(value = "用户唯一代码")
    private String userCode;

    /**
     * 关联用户信息id
     */
    @ExcelProperty(value = "关联用户信息id")
    private Long userinfoId;

    /**
     * 上级用户代码
     */
    @ExcelProperty(value = "上级用户代码")
    private String userPcode;

    /**
     * 手机号
     */
    @ExcelProperty(value = "手机号")
    private String userMobile;

    /**
     * 绑定手机号
     */
    @ExcelProperty(value = "绑定手机号")
    private String userBindMobile;

    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱")
    private String userEmail;

    /**
     * 登录密码
     */
    @ExcelProperty(value = "登录密码")
    private String userPassword;

    /**
     * 支付密码
     */
    @ExcelProperty(value = "支付密码")
    private String userPayPassword;

    /**
     * 客服电话
     */
    @ExcelProperty(value = "客服电话")
    private String userCustomerServicePhone;

    /**
     * 是否启用：0禁用，1启用
     */
    @ExcelProperty(value = "是否启用：0禁用，1启用")
    private Long isEnabled;


}
