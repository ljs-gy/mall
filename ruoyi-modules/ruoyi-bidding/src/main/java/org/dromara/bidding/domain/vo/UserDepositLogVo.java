package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.UserDepositLog;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 保证金流水视图对象 user_deposit_log
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = UserDepositLog.class)
public class UserDepositLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 流水号
     */
    @ExcelProperty(value = "流水号")
    private String logCode;

    /**
     * 用户买家ID
     */
    @ExcelProperty(value = "用户买家ID")
    private Long userId;

    /**
     * 竞价场次ID
     */
    @ExcelProperty(value = "竞价场次ID")
    private Long sessionId;

    /**
     * 关联的具体标的条目ID
     */
    @ExcelProperty(value = "关联的具体标的条目ID")
    private Long itemId;

    /**
     * 本次变动金额
     */
    @ExcelProperty(value = "本次变动金额")
    private BigDecimal amount;

    /**
     * 变动财务方向：1-线下电汇入账交纳, 2-申请参与竞价被冻结锁定, 3-未中标全自动释放退回, 4-罚没, 5-保证金转为定金抵扣最终订单货款, 6-收取佣金直接扣减, 7-佣金返还
     */
    @ExcelProperty(value = "变动财务方向：1-线下电汇入账交纳, 2-申请参与竞价被冻结锁定, 3-未中标全自动释放退回, 4-罚没, 5-保证金转为定金抵扣最终订单货款, 6-收取佣金直接扣减, 7-佣金返还")
    private Integer direction;

    /**
     * 变动前可用保证金余额
     */
    @ExcelProperty(value = "变动前可用保证金余额")
    private BigDecimal beforeRefundableDeposit;

    /**
     * 变动后可用保证金余额
     */
    @ExcelProperty(value = "变动后可用保证金余额")
    private BigDecimal afterRefundableDeposit;

    /**
     * 变动前冻结保证金金额
     */
    @ExcelProperty(value = "变动前冻结保证金金额")
    private BigDecimal beforeAuditingDeposit;

    /**
     * 变动后冻结保证金金额
     */
    @ExcelProperty(value = "变动后冻结保证金金额")
    private BigDecimal afterAuditingDeposit;

    /**
     * 关联外部唯一号
     */
    @ExcelProperty(value = "关联外部唯一号")
    private String bizNo;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 审计操作人
     */
    @ExcelProperty(value = "审计操作人")
    private String operatorUser;

}