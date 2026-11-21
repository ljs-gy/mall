package org.dromara.order.domain.vo;

import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.order.domain.OrderItem;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 订单明细视图对象 oc_order_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = OrderItem.class)
public class OrderItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明细主键ID
     */
    @ExcelProperty(value = "明细主键ID")
    private Long id;

    /**
     * 关联订单号
     */
    @ExcelProperty(value = "关联订单号")
    private String orderNo;

    /**
     * 商品 SPU ID
     */
    @ExcelProperty(value = "商品 SPU ID")
    private Long spuId;

    /**
     * 商品 SKU ID
     */
    @ExcelProperty(value = "商品 SKU ID")
    private Long skuId;

    /**
     * 货品编号
     */
    @ExcelProperty(value = "货品编号")
    private String skuCode;

    /**
     * 物权编码
     */
    @ExcelProperty(value = "物权编码")
    private String memberCode;

    /**
     * 商品标题
     */
    @ExcelProperty(value = "商品标题")
    private String goodsTitle;

    /**
     * 规格文本
     */
    @ExcelProperty(value = "规格文本")
    private String specText;

    /**
     * 主图
     */
    @ExcelProperty(value = "主图")
    private String coverImage;

    /**
     * 主图Url
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "coverImage")
    private String coverImageUrl;
    /**
     * 成交时单价（元）
     */
    @ExcelProperty(value = "成交时单价")
    private BigDecimal priceSales;

    /**
     * 单件基准折后实付均价（元，向下截断到分）
     */
    @ExcelProperty(value = "单件折后实付均价")
    private BigDecimal sharePrice;

    /**
     * 购买数量
     */
    @ExcelProperty(value = "购买数量")
    private Long quantity;

    /**
     * 是否为赠品: 0否(主品), 1是(赠品)
     */
    @ExcelProperty(value = "是否为赠品: 0否(主品), 1是(赠品)")
    private Long isGift;

    /**
     * 触发赠品的阶梯门槛规则ID
     */
    @ExcelProperty(value = "阶梯门槛规则ID")
    private Long tierRuleId;

    /**
     * 小计金额（元）
     */
    @ExcelProperty(value = "小计金额")
    private BigDecimal subTotal;

    /**
     * 当前明细行最终实付金额（元）
     */
    @ExcelProperty(value = "当前明细行最终实付金额")
    private BigDecimal realPayAmount;

    /** 售后申请中数量 */
    @ExcelProperty(value = "售后申请中数量")
    private Integer refundingQty;

    /** 售后申请中金额 */
    @ExcelProperty(value = "售后申请中金额")
    private BigDecimal refundingAmount;

    /** 已退款成功数量 */
    @ExcelProperty(value = "已退款成功数量")
    private Integer refundedQty;

    /** 已退款成功金额 */
    @ExcelProperty(value = "已退款成功金额")
    private BigDecimal refundedAmount;

}
