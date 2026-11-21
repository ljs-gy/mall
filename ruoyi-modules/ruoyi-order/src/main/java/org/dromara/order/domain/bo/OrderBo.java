package org.dromara.order.domain.bo;

import org.dromara.order.domain.Order;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 订单主业务对象 oc_order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Order.class, reverseConvertGenerate = false)
public class OrderBo extends BaseEntity {

    /**
     * 订单主键ID
     */
    @NotNull(message = "订单主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 主订单号
     */
    @NotBlank(message = "主订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String parentOrderNo;

    /**
     * 支付流水号
     */
    @NotBlank(message = "支付流水号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payTradeNo;

    /**
     * 渠道编码
     */
    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /**
     * 物权编码
     */
    @NotBlank(message = "物权编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    /**
     * 买家ID
     */
    @NotNull(message = "买家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    private String memberBcode;

    /**
     * 下单时会员等级 ID
     */
    private Long userLevelId;

    /**
     * 订单状态：10待支付，20待发货，30待收货，40已完成，50已取消
     */
    @NotNull(message = "订单状态：10待支付，20待发货，30待收货，40已完成，50已取消不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orderStatus;

    /**
     * 支付状态: 0未支付, 1已支付, 2全额退款, 3部分退款
     */
    @NotNull(message = "支付状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long payStatus;

    /**
     * 商品总金额（元）
     */
    @NotNull(message = "商品总金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal goodsTotalAmount;

    /**
     * 运费金额（元）
     */
    @NotNull(message = "运费金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal freightAmount;

    /**
     * 优惠/优惠券抵扣金额（元）
     */
    @NotNull(message = "优惠/优惠券抵扣金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal discountAmount;

    /**
     * 实际应付总金额（元）
     */
    @NotNull(message = "实际应付总金额不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotBlank(message = "收货人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeName;

    /**
     * 收货人手机
     */
    @NotBlank(message = "收货人手机不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeMobile;

    /**
     * 收货完整地址
     */
    @NotBlank(message = "收货完整地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String receiverAddressText;

    /**
     * 买家备注
     */
    private String buyerRemark;


}
