package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 保证金流水对象 user_deposit_log
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_deposit_log")
public class UserDepositLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 流水号
     */
    private String logCode;

    /**
     * 用户买家ID
     */
    private Long userId;

    /**
     * 竞价场次ID
     */
    private Long sessionId;

    /**
     * 关联的具体标的条目ID
     */
    private Long itemId;

    /**
     * 本次变动金额
     */
    private BigDecimal amount;

    /**
     * 变动财务方向：1-线下电汇入账交纳, 2-申请参与竞价被冻结锁定, 3-未中标全自动释放退回, 4-罚没, 5-保证金转为定金抵扣最终订单货款, 6-收取佣金直接扣减, 7-佣金返还
     */
    private Integer direction;

    /**
     * 变动前可用保证金余额
     */
    private BigDecimal beforeRefundableDeposit;

    /**
     * 变动后可用保证金余额
     */
    private BigDecimal afterRefundableDeposit;

    /**
     * 变动前冻结保证金金额
     */
    private BigDecimal beforeAuditingDeposit;

    /**
     * 变动后冻结保证金金额
     */
    private BigDecimal afterAuditingDeposit;

    /**
     * 关联外部唯一号
     */
    private String bizNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 审计操作人
     */
    private String operatorUser;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 更新者（表无此列）
     */
    @TableField(exist = false)
    private Long updateBy;

    /**
     * 更新时间（表无此列）
     */
    @TableField(exist = false)
    private Date updateTime;

}