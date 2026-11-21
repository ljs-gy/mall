package org.dromara.user.domain.bo;

import org.dromara.user.domain.RechargeApply;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 充值申请业务对象 um_recharge_apply
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = RechargeApply.class, reverseConvertGenerate = false)
public class RechargeApplyBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 充值申请单号
     */
    @NotBlank(message = "充值申请单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applyNo;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 用户编码
     */
    @NotBlank(message = "用户编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userCode;

    /**
     * 用户名称
     */
    @NotBlank(message = "用户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userinfoName;

    /**
     * 充值入账的目标资金账户号
     */
    @NotBlank(message = "充值入账的目标资金账户号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accountNo;

    /**
     * 充值金额(元)
     */
    @NotNull(message = "充值金额(元)不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal rechargeAmount;

    /**
     * 凭证号码
     */
    @NotBlank(message = "凭证号码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String voucherNo;

    /**
     * 合同号
     */
    private String contractNo;

    /**
     * 凭证图片URL
     */
    @NotBlank(message = "凭证图片URL不能为空", groups = { AddGroup.class, EditGroup.class })
    private String voucherUrls;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 审核状态：0待审核，1审核通过，2审核驳回
     */
    @NotNull(message = "审核状态：0待审核，1审核通过，2审核驳回不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditStatus;

    /**
     * 审核完成时间
     */
    private Date auditTime;

    /**
     * 审核操作人ID
     */
    private Long auditUserId;

    /**
     * 审核操作人姓名
     */
    private String auditUserName;

    /**
     * 审核备注
     */
    private String auditRemark;


}
