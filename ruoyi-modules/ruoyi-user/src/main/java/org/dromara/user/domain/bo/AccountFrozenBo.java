package org.dromara.user.domain.bo;

import org.dromara.user.domain.AccountFrozen;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资金账户冻结明细业务对象 um_account_frozen
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AccountFrozen.class, reverseConvertGenerate = false)
public class AccountFrozenBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联资金账户号
     */
    @NotBlank(message = "关联资金账户号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accountNo;

    /**
     * 本次冻结金额
     */
    @NotNull(message = "本次冻结金额不能为空", groups = { AddGroup.class, EditGroup.class })
    @DecimalMin(value = "0.01", message = "冻结金额必须大于0", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal frozenAmount;

    /**
     * 已解冻/已划扣金额
     */
    private BigDecimal unfrozenAmount;

    /**
     * 冻结业务类型: WITHDRAW / ORDER_LOCK / DEPOSIT_LOCK
     */
    @NotBlank(message = "冻结业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bizType;

    /**
     * 业务关联单号
     */
    @NotBlank(message = "业务关联单号不能为空", groups = { AddGroup.class, EditGroup.class })
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
