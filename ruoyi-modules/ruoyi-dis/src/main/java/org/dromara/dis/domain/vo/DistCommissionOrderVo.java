package org.dromara.dis.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.dis.domain.DistCommissionOrder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分佣业务头表视图对象 dist_commission_order
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistCommissionOrder.class)
public class DistCommissionOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键")
    private Long id;

    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    @ExcelProperty(value = "分佣单号")
    private String commissionNo;

    @ExcelProperty(value = "订单号")
    private String orderNo;

    @ExcelProperty(value = "下单人")
    private Long buyerUserinfoId;

    @ExcelProperty(value = "计佣金额")
    private BigDecimal orderAmount;

    @ExcelProperty(value = "备注")
    private String remark;
}
