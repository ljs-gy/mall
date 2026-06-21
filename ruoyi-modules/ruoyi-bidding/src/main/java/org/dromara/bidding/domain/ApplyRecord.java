package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 报名记录对象 bid_apply_record
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_apply_record")
public class ApplyRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 报名全局唯一审批单号
     */
    private String applyNo;

    /**
     * 关联竞价场次主表ID
     */
    private Long sessionId;

    /**
     * 买家用户ID
     */
    private Long userId;

    /**
     * 买家上传的特定资质证明文件
     */
    private String qualificationProofs;

    /**
     * 本场应交保证金总额
     */
    private BigDecimal needDepositAmount;

    /**
     * 可用保证金余额
     */
    private BigDecimal refundableDeposit;

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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}