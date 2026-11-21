package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 用户申请审核记录对象 um_user_apply
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_user_apply")
public class UserApply extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联用户信息ID
     */
    private Long userinfoId;

    /**
     * 申请身份类型：buy(采购商), merchant(商家), supplier(供应商)，dis(分销员)
     */
    private String applyIdentityType;

    /**
     * 申请动作类型：1-首次入驻认证, 2-信息/资质变更修改
     */
    private Long applyType;

    /**
     * 用户类型：1个人，2企业
     */
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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}
