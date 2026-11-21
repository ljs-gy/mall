package org.dromara.user.domain.vo;

import org.dromara.user.domain.AccountFrozen;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资金账户冻结明细视图对象 um_account_frozen
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AccountFrozen.class)
public class AccountFrozenVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 关联资金账户号
     */
    @ExcelProperty(value = "关联资金账户号")
    private String accountNo;

    /**
     * 本次冻结金额
     */
    @ExcelProperty(value = "本次冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 已解冻/已划扣金额
     */
    @ExcelProperty(value = "已解冻/已划扣金额")
    private BigDecimal unfrozenAmount;

    /**
     * 冻结业务类型
     */
    @ExcelProperty(value = "冻结业务类型")
    private String bizType;

    /**
     * 业务关联单号
     */
    @ExcelProperty(value = "业务关联单号")
    private String bizOrderNo;

    /**
     * 状态：1冻结中，2已解冻，3已划扣
     */
    @ExcelProperty(value = "状态")
    private Long status;

    /**
     * 最近一次解冻/划扣时间
     */
    @ExcelProperty(value = "解冻/划扣时间")
    private Date unfrozenTime;

    /**
     * 解冻/划扣关联业务单号
     */
    @ExcelProperty(value = "解冻/划扣业务单号")
    private String unfrozenBizNo;

    /**
     * 冻结备注
     */
    @ExcelProperty(value = "冻结备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}
