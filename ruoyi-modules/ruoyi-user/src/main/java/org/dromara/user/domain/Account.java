package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 用户资金账户对象 um_account
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_account")
public class Account extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联um_userinfo主键ID
     */
    private Long userinfoId;

    /**
     * 资金系统唯一账户号
     */
    private String accountNo;

    /**
     * 账户名称（基本户、保证金户、分销佣金户等）
     */
    private String accountName;

    /**
     * 账户类型：BASIC(基本户), DEPOSIT(保证金户), COMMISSION(佣金户)
     */
    private String accountType;

    /**
     * 币种：CNY, USD
     */
    private String currency;

    /**
     * 账户总余额
     */
    private BigDecimal balance;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 冻结余额
     */
    private BigDecimal frozenBalance;

    /**
     * 状态：1正常，2冻结，3注销
     */
    private Long status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Long version;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}
