package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.BidLog;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 出价流水视图对象 bid_bid_log
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BidLog.class)
public class BidLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 出价流水号
     */
    @ExcelProperty(value = "出价流水号")
    private String bidLogCode;

    /**
     * 竞价场次ID
     */
    @ExcelProperty(value = "竞价场次ID")
    private Long sessionId;

    /**
     * 标的明细ID
     */
    @ExcelProperty(value = "标的明细ID")
    private Long itemId;

    /**
     * 买家用户ID
     */
    @ExcelProperty(value = "买家用户ID")
    private Long userId;

    /**
     * 买家报出的单位重量溢价金额
     */
    @ExcelProperty(value = "单位重量溢价金额")
    private BigDecimal bidPrice;

    /**
     * 本次出价折算单价
     */
    @ExcelProperty(value = "本次出价折算单价")
    private BigDecimal bidPriceUnit;

    /**
     * 折算出的成交总金额
     */
    @ExcelProperty(value = "成交总金额")
    private BigDecimal bidTotalMoney;

    /**
     * 应标意向数量/重量
     */
    @ExcelProperty(value = "应标意向数量/重量")
    private BigDecimal goodsQuantity;

    /**
     * 精准出价时刻
     */
    @ExcelProperty(value = "精准出价时刻")
    private Date bidTime;

    /**
     * 出价状态：1-出价成功暂列领先, 2-已被超越, 3-最终中标, 4-无效出价
     */
    @ExcelProperty(value = "出价状态：1-出价成功暂列领先, 2-已被超越, 3-最终中标, 4-无效出价")
    private Integer bidStatus;

}