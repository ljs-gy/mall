package org.dromara.user.domain.vo;

import org.dromara.user.domain.Account;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户资金账户视图对象 um_account
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Account.class)
public class AccountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "用户信息ID")
    private Long userinfoId;

    @ExcelProperty(value = "资金账户号")
    private String accountNo;

    @ExcelProperty(value = "账户名称")
    private String accountName;

    @ExcelProperty(value = "账户类型")
    private String accountType;

    @ExcelProperty(value = "币种")
    private String currency;

    @ExcelProperty(value = "账户总余额")
    private BigDecimal balance;

    @ExcelProperty(value = "可用余额")
    private BigDecimal availableBalance;

    @ExcelProperty(value = "冻结余额")
    private BigDecimal frozenBalance;

    @ExcelProperty(value = "状态")
    private Long status;

}
