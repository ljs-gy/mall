package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 订单主对象 oc_order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_order")
public class Order extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 主订单号
     */
    private String parentOrderNo;

    /**
     * 支付流水号
     */
    private String payTradeNo;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 物权编码
     */
    private String memberCode;

    /**
     * 买家ID
     */
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    private String memberBcode;

    /**
     * 下单时会员等级 ID（营销人群快照，支付发 MQ 时使用）
     */
    private Long userLevelId;

    /**
     * 订单状态：10待支付，20待发货，30待收货，40已完成，50已取消
     */
    private Long orderStatus;

    /**
     * 支付状态: 0未支付, 1已支付, 2全额退款, 3部分退款
     */
    private Long payStatus;

    /**
     * 商品总金额（元）
     */
    private BigDecimal goodsTotalAmount;

    /**
     * 运费金额（元）
     */
    private BigDecimal freightAmount;

    /**
     * 优惠/优惠券抵扣金额（元）
     */
    private BigDecimal discountAmount;

    /**
     * 实际应付总金额（元）
     */
    private BigDecimal payAmount;

    /**
     * 实际支付时间
     */
    private Date payTime;

    /**
     * 卖家发货时间
     */
    private Date shippingTime;

    /**
     * 确认收货时间
     */
    private Date receiveTime;

    /**
     * 收货人姓名
     */
    private String consigneeName;

    /**
     * 收货人手机
     */
    private String consigneeMobile;

    /**
     * 收货完整地址
     */
    private String receiverAddressText;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;


}
