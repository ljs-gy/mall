package org.dromara.user.domain.bo;

import org.dromara.user.domain.AccountJournal;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 用户账务动账流水业务对象 um_account_journal
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AccountJournal.class, reverseConvertGenerate = false)
public class AccountJournalBo extends BaseEntity {

    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "流水号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String journalNo;

    @NotBlank(message = "关联资金账户号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accountNo;

    @NotBlank(message = "动账业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bizType;

    @NotBlank(message = "外部关联业务单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bizOrderNo;

    @NotNull(message = "变动方向不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long direction;

    @NotNull(message = "变动金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    @NotNull(message = "变动前总余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal beforeBalance;

    @NotNull(message = "变动后总余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal afterBalance;

}
