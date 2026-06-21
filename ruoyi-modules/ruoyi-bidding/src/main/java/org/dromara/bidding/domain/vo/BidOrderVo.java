package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.BidOrder;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 竞价订单视图对象 bid_order
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BidOrder.class)
public class BidOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 竞价订单号
     */
    @ExcelProperty(value = "竞价订单号")
    private String orderNo;

    /**
     * 所属竞价场次公告ID
     */
    @ExcelProperty(value = "所属竞价场次公告ID")
    private Long sessionId;

    /**
     * 中标明细条目ID
     */
    @ExcelProperty(value = "中标明细条目ID")
    private Long itemId;

    /**
     * 中标买家用户ID
     */
    @ExcelProperty(value = "中标买家用户ID")
    private Long userId;

    /**
     * 中标成交单价
     */
    @ExcelProperty(value = "中标成交单价")
    private BigDecimal strikePrice;

    /**
     * 成交总重量/总数量
     */
    @ExcelProperty(value = "成交总重量/总数量")
    private BigDecimal quantity;

    /**
     * 商品总价
     */
    @ExcelProperty(value = "商品总价")
    private BigDecimal totalGoodsAmount;

    /**
     * 保证金转货款抵扣金额
     */
    @ExcelProperty(value = "保证金转货款抵扣金额")
    private BigDecimal depositOffsetAmount;

    /**
     * 平台佣金服务费
     */
    @ExcelProperty(value = "平台佣金服务费")
    private BigDecimal commissionAmount;

    /**
     * 最终买家应补尾款
     */
    @ExcelProperty(value = "最终买家应补尾款")
    private BigDecimal finalPendingAmount;

    /**
     * 履约状态：0-中标待补尾款及佣金, 1-尾款佣金双清结, 2-已出库, 3-已收货, 4-超时未补尾
     */
    @ExcelProperty(value = "履约状态：0-中标待补尾款及佣金, 1-尾款佣金双清结, 2-已出库, 3-已收货, 4-超时未补尾")
    private Integer orderStatus;

    /**
     * 清结时间
     */
    @ExcelProperty(value = "清结时间")
    private Date payTime;

    /**
     * 买家补尾款倒计时
     */
    @ExcelProperty(value = "买家补尾款倒计时")
    private Date expireTime;

}