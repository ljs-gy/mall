package org.dromara.dis.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.dis.domain.DistCommissionItem;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 分佣明细视图对象 dist_commission_item
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistCommissionItem.class)
public class DistCommissionItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键")
    private Long id;

    @ExcelProperty(value = "分佣头表ID")
    private Long commissionOrderId;

    @ExcelProperty(value = "分佣单号")
    private String commissionNo;

    @ExcelProperty(value = "订单号")
    private String orderNo;

    @ExcelProperty(value = "受益人")
    private Long beneficiaryUserinfoId;

    @ExcelProperty(value = "层级")
    private Integer level;

    @ExcelProperty(value = "比例")
    private BigDecimal rate;

    @ExcelProperty(value = "佣金金额")
    private BigDecimal amount;

    @ExcelProperty(value = "状态")
    private Integer status;

    @ExcelProperty(value = "结算时间")
    private Date settleTime;

    @ExcelProperty(value = "取消时间")
    private Date cancelTime;

    @ExcelProperty(value = "取消原因")
    private String cancelReason;

    @ExcelProperty(value = "入账流水号")
    private String accountJournalNo;
}
