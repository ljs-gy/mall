package org.dromara.user.domain.bo;

import org.dromara.user.domain.UserApply;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户申请审核记录业务对象 um_user_apply
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserApply.class, reverseConvertGenerate = false)
public class UserApplyBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联用户信息ID
     */
    @NotNull(message = "关联用户信息ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 申请身份类型：buy(采购商), merchant(商家), supplier(供应商)，dis(分销员)
     */
    @NotBlank(message = "申请身份类型：buy(采购商), merchant(商家), supplier(供应商)，dis(分销员)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applyIdentityType;

    /**
     * 申请动作类型：1-首次入驻认证, 2-信息/资质变更修改
     */
    @NotNull(message = "申请动作类型：1-首次入驻认证, 2-信息/资质变更修改不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long applyType;

    /**
     * 用户类型：1个人，2企业
     */
    @NotNull(message = "用户类型：1个人，2企业不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userType;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 营业执照照片
     */
    private String businessLicenseUrl;

    /**
     * 法人姓名
     */
    private String legalPersonName;

    /**
     * 法人身份证号
     */
    private String legalPersonIdCard;

    /**
     * 身份证正面照片
     */
    private String idCardFrontUrl;

    /**
     * 身份证反面照片
     */
    private String idCardBackUrl;

    /**
     * 其他资质文件
     */
    private String qualificationFiles;

    /**
     * 申请说明
     */
    private String applyRemark;

    /**
     * 审核状态：0待审核，1审核通过，2审核驳回
     */
    @NotNull(message = "审核状态：0待审核，1审核通过，2审核驳回不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditStatus;

    /**
     * 审核意见
     */
    private String auditRemark;

    /**
     * 审核操作人ID
     */
    private Long auditUserId;

    /**
     * 审核操作人姓名
     */
    private String auditUserName;

    /**
     * 审核时指定的部门ID
     */
    private Long assignedDeptId;

    /**
     * 审核时指定的业务员ID
     */
    private Long assignedSalesmanId;

    /**
     * 审核完成时间
     */
    private Date auditTime;


}
