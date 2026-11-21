package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资金账户冻结明细对象 um_account_frozen
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_account_frozen")
public class AccountFrozen extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联资金账户号
     */
    private String accountNo;

    /**
     * 本次冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 已解冻/已划扣金额(支持部分解冻)
     */
    private BigDecimal unfrozenAmount;

    /**
     * 冻结业务类型: WITHDRAW(提现), ORDER_LOCK(订单担保), DEPOSIT_LOCK(保证金违规冻结)
     */
    private String bizType;

    /**
     * 业务关联单号
     */
    private String bizOrderNo;

    /**
     * 状态：1冻结中，2已解冻，3已划扣
     */
    private Long status;

    /**
     * 最近一次解冻/划扣时间
     */
    private Date unfrozenTime;

    /**
     * 解冻/划扣关联业务单号
     */
    private String unfrozenBizNo;

    /**
     * 冻结备注
     */
    private String remark;

}
