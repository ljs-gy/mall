package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 发货单对象 oc_shipping_order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_shipping_order")
public class ShippingOrder extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 发货单主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 发货单号
     */
    private String shippingNo;

    /**
     * 关联店铺子订单号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    private String memberBcode;

    /**
     * 物权编码
     */
    private String memberCode;

    /**
     * 发货状态：10待发货，20已发货，30已签收
     */
    private Long shippingStatus;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 收货人姓名
     */
    private String consigneeName;

    /**
     * 收货人手机号
     */
    private String consigneeMobile;

    /**
     * 收货地址快照
     */
    private String receiverAddressText;

    /**
     * 实际发货时间
     */
    private Date shippingTime;

    /**
     * 实际签收时间
     */
    private Date receiveTime;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;

}
