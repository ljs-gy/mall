package org.dromara.order.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.order.domain.Order;
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
 * 订单主视图对象 oc_order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Order.class)
public class OrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单主键ID
     */
    @ExcelProperty(value = "订单主键ID")
    private Long id;

    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    private String orderNo;

    /**
     * 主订单号
     */
    @ExcelProperty(value = "主订单号")
    private String parentOrderNo;

    /**
     * 支付流水号
     */
    @ExcelProperty(value = "支付流水号")
    private String payTradeNo;

    /**
     * 渠道编码
     */
    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    /**
     * 物权编码
     */
    @ExcelProperty(value = "物权编码")
    private String memberCode;

    /**
     * 买家ID
     */
    @ExcelProperty(value = "买家ID")
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    @ExcelProperty(value = "买家业务编码")
    private String memberBcode;

    /**
     * 下单时会员等级 ID
     */
    @ExcelProperty(value = "会员等级ID")
    private Long userLevelId;

    /**
     * 订单状态：10待支付，20待发货，30待收货，40已完成，50已取消
     */
    @ExcelProperty(value = "订单状态：10待支付，20待发货，30待收货，40已完成，50已取消")
    private Long orderStatus;

    /**
     * 支付状态: 0未支付, 1已支付, 2全额退款, 3部分退款
     */
    @ExcelProperty(value = "支付状态")
    private Long payStatus;

    /**
     * 商品总金额（元）
     */
    @ExcelProperty(value = "商品总金额")
    private BigDecimal goodsTotalAmount;

    /**
     * 运费金额（元）
     */
    @ExcelProperty(value = "运费金额")
    private BigDecimal freightAmount;

    /**
     * 优惠/优惠券抵扣金额（元）
     */
    @ExcelProperty(value = "优惠/优惠券抵扣金额")
    private BigDecimal discountAmount;

    /**
     * 实际应付总金额（元）
     */
    @ExcelProperty(value = "实际应付总金额")
    private BigDecimal payAmount;

    /**
     * 实际支付时间
     */
    @ExcelProperty(value = "实际支付时间")
    private Date payTime;

    /**
     * 卖家发货时间
     */
    @ExcelProperty(value = "卖家发货时间")
    private Date shippingTime;

    /**
     * 确认收货时间
     */
    @ExcelProperty(value = "确认收货时间")
    private Date receiveTime;

    /**
     * 收货人姓名
     */
    @ExcelProperty(value = "收货人姓名")
    private String consigneeName;

    /**
     * 收货人手机
     */
    @ExcelProperty(value = "收货人手机")
    private String consigneeMobile;

    /**
     * 收货完整地址
     */
    @ExcelProperty(value = "收货完整地址")
    private String receiverAddressText;

    /**
     * 买家备注
     */
    @ExcelProperty(value = "买家备注")
    private String buyerRemark;


}
