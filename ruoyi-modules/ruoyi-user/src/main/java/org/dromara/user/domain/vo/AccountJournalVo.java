package org.dromara.user.domain.vo;

import org.dromara.user.domain.AccountJournal;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户账务动账流水视图对象 um_account_journal
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AccountJournal.class)
public class AccountJournalVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "流水号")
    private String journalNo;

    @ExcelProperty(value = "关联资金账户号")
    private String accountNo;

    @ExcelProperty(value = "动账业务类型")
    private String bizType;

    @ExcelProperty(value = "外部关联业务单号")
    private String bizOrderNo;

    @ExcelProperty(value = "变动方向")
    private Long direction;

    @ExcelProperty(value = "变动金额")
    private BigDecimal amount;

    @ExcelProperty(value = "变动前总余额")
    private BigDecimal beforeBalance;

    @ExcelProperty(value = "变动后总余额")
    private BigDecimal afterBalance;

}
