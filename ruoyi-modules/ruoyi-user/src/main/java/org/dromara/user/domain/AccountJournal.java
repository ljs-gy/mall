package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 用户账务动账流水对象 um_account_journal
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_account_journal")
public class AccountJournal extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 流水号（全局唯一，幂等键）
     */
    private String journalNo;

    /**
     * 关联资金账户号
     */
    private String accountNo;

    /**
     * 动账业务类型: RECHARGE(充值), WITHDRAW(提现), SETTLE(交易结算), COMMISSION(佣金发放)
     */
    private String bizType;

    /**
     * 外部关联业务单号
     */
    private String bizOrderNo;

    /**
     * 变动方向：1增加(+)，2减少(-)
     */
    private Long direction;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    /**
     * 变动前总余额
     */
    private BigDecimal beforeBalance;

    /**
     * 变动后总余额
     */
    private BigDecimal afterBalance;

}
