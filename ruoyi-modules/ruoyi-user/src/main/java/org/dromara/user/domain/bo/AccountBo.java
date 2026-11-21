package org.dromara.user.domain.bo;

import org.dromara.user.domain.Account;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 用户资金账户业务对象 um_account
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Account.class, reverseConvertGenerate = false)
public class AccountBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联um_userinfo主键ID
     */
    @NotNull(message = "关联um_userinfo主键ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 资金系统唯一账户号
     */
    @NotBlank(message = "资金系统唯一账户号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accountNo;

    /**
     * 账户名称（基本户、保证金户、分销佣金户等）
     */
    @NotBlank(message = "账户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accountName;

    /**
     * 账户类型：BASIC(基本户), DEPOSIT(保证金户), COMMISSION(佣金户)
     */
    @NotBlank(message = "账户类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accountType;

    /**
     * 币种：CNY, USD
     */
    @NotBlank(message = "币种不能为空", groups = { AddGroup.class, EditGroup.class })
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

}
