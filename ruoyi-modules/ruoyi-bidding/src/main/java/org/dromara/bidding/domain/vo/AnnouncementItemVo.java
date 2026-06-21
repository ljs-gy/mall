package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.AnnouncementItem;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 竞价标的视图对象 bid_announcement_item
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AnnouncementItem.class)
public class AnnouncementItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联场次活动主表ID
     */
    @ExcelProperty(value = "关联场次活动主表ID")
    private Long sessionId;

    /**
     * 标的条目全网唯一业务编码
     */
    @ExcelProperty(value = "标的条目全网唯一业务编码")
    private String itemCode;

    /**
     * 标的属性：1-打包包裹（多商品组合）, 2-标准单品（单SKU）
     */
    @ExcelProperty(value = "标的属性：1-打包包裹（多商品组合）, 2-标准单品（单SKU）")
    private Integer itemType;

    /**
     * 关联物权资源库源头ID
     */
    @ExcelProperty(value = "关联物权资源库源头ID")
    private Long resourceId;

    /**
     * 包裹描述或商品单品展示名称
     */
    @ExcelProperty(value = "包裹描述或商品单品展示名称")
    private String itemName;

    /**
     * 起拍基价
     */
    @ExcelProperty(value = "起拍基价")
    private BigDecimal pricesetNPrice;

    /**
     * 标的可供总数量/总重量
     */
    @ExcelProperty(value = "标的可供总数量/总重量")
    private BigDecimal goodsQuantity;

    /**
     * 起拍初始总金额
     */
    @ExcelProperty(value = "起拍初始总金额")
    private BigDecimal pricesetPrefPrice;

    /**
     * 加价幅度
     */
    @ExcelProperty(value = "加价幅度")
    private BigDecimal auctionRuleAddPrice;

    /**
     * 单次最高出价限制，0为不限制
     */
    @ExcelProperty(value = "单次最高出价限制，0为不限制")
    private BigDecimal maxBidLimit;

    /**
     * 属商品保证金金额
     */
    @ExcelProperty(value = "属商品保证金金额")
    private BigDecimal itemDepositAmount;

    /**
     * 当前最高溢价
     */
    @ExcelProperty(value = "当前最高溢价")
    private BigDecimal auctionPriceNow;

    /**
     * 最高成交单价
     */
    @ExcelProperty(value = "最高成交单价")
    private BigDecimal auctionGinfoPrice;

    /**
     * 当前最新成交总金额
     */
    @ExcelProperty(value = "当前最新成交总金额")
    private BigDecimal auctionGinfoMoney;

    /**
     * 第一名买家用户ID
     */
    @ExcelProperty(value = "第一名买家用户ID")
    private Long currentMaxUserId;

    /**
     * 累计有效被出价抢占次数
     */
    @ExcelProperty(value = "累计有效被出价抢占次数")
    private Integer totalBidCount;

    /**
     * 条目运行状态：0-未开拍, 1-竞价中, 2-已落槌成交, 3-到期流拍, 4-中标后超时未补尾款触发违约
     */
    @ExcelProperty(value = "条目运行状态：0-未开拍, 1-竞价中, 2-已落槌成交, 3-到期流拍, 4-中标后超时未补尾款触发违约")
    private Integer itemStatus;

    /**
     * 高并发防超卖与并发覆盖乐观锁版本号
     */
    @ExcelProperty(value = "高并发防超卖与并发覆盖乐观锁版本号")
    private Integer version;

}