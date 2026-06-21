package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.UserDepositLog;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 保证金流水业务对象 user_deposit_log
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserDepositLog.class, reverseConvertGenerate = false)
public class UserDepositLogBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "流水号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 流水号
     */
    private String logCode;

    @NotNull(message = "用户买家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 用户买家ID
     */
    private Long userId;

    @NotNull(message = "竞价场次ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价场次ID
     */
    private Long sessionId;

    /**
     * 关联的具体标的条目ID
     */
    private Long itemId;

    @NotNull(message = "本次变动金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 本次变动金额
     */
    private BigDecimal amount;

    @NotNull(message = "变动财务方向：1-线下电汇入账交纳, 2-申请参与竞价被冻结锁定, 3-未中标全自动释放退回, 4-罚没, 5-保证金转为定金抵扣最终订单货款, 6-收取佣金直接扣减, 7-佣金返还不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 变动财务方向：1-线下电汇入账交纳, 2-申请参与竞价被冻结锁定, 3-未中标全自动释放退回, 4-罚没, 5-保证金转为定金抵扣最终订单货款, 6-收取佣金直接扣减, 7-佣金返还
     */
    private Integer direction;

    @NotNull(message = "变动前可用保证金余额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 变动前可用保证金余额
     */
    private BigDecimal beforeRefundableDeposit;

    @NotNull(message = "变动后可用保证金余额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 变动后可用保证金余额
     */
    private BigDecimal afterRefundableDeposit;

    @NotNull(message = "变动前冻结保证金金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 变动前冻结保证金金额
     */
    private BigDecimal beforeAuditingDeposit;

    @NotNull(message = "变动后冻结保证金金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 变动后冻结保证金金额
     */
    private BigDecimal afterAuditingDeposit;

    @NotBlank(message = "关联外部唯一号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联外部唯一号
     */
    private String bizNo;

    /**
     * 备注
     */
    private String remark;

    @NotBlank(message = "审计操作人不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 审计操作人
     */
    private String operatorUser;

}