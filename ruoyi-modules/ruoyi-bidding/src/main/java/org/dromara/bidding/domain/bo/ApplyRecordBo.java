package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.ApplyRecord;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 报名记录业务对象 bid_apply_record
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ApplyRecord.class, reverseConvertGenerate = false)
public class ApplyRecordBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "报名全局唯一审批单号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 报名全局唯一审批单号
     */
    private String applyNo;

    @NotNull(message = "关联竞价场次主表ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联竞价场次主表ID
     */
    private Long sessionId;

    @NotNull(message = "买家用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 买家用户ID
     */
    private Long userId;

    /**
     * 买家上传的特定资质证明文件
     */
    private String qualificationProofs;

    @NotNull(message = "本场应交保证金总额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 本场应交保证金总额
     */
    private BigDecimal needDepositAmount;

    @NotNull(message = "可用保证金余额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 可用保证金余额
     */
    private BigDecimal refundableDeposit;

    @NotNull(message = "冻结中的保证金不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 冻结中的保证金
     */
    private BigDecimal auditingDeposit;

    /**
     * 买家在PC商城上传的线下银行电汇打款凭证水单图片URL
     */
    private String payProofImage;

    /**
     * 买家电汇打款备注/说明
     */
    private String payRemark;

    @NotNull(message = "准入审批状态：0-待审核, 1-审核通过, 2-审核拒绝退回不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 准入审批状态：0-待审核, 1-审核通过, 2-审核拒绝退回
     */
    private Integer auditStatus;

    /**
     * 审核操作人员工号
     */
    private String auditUser;

    /**
     * 审批拒绝/退回原因说明
     */
    private String auditOpinion;

}