package org.dromara.order.domain.bo;

import org.dromara.order.domain.ShippingOrder;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 发货单业务对象 oc_shipping_order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ShippingOrder.class, reverseConvertGenerate = false)
public class ShippingOrderBo extends BaseEntity {

    /**
     * 发货单主键ID
     */
    @NotNull(message = "发货单主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 发货单号
     */
    @NotBlank(message = "发货单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String shippingNo;

    /**
     * 关联店铺子订单号
     */
    @NotBlank(message = "关联店铺子订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

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
     * 物权编码
     */
    @NotBlank(message = "物权编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    /**
     * 发货状态：10待发货，20已发货，30已签收
     */
    @NotNull(message = "发货状态：10待发货，20已发货，30已签收不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotBlank(message = "收货人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeName;

    /**
     * 收货人手机号
     */
    @NotBlank(message = "收货人手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeMobile;

    /**
     * 收货地址快照
     */
    @NotBlank(message = "收货地址快照不能为空", groups = { AddGroup.class, EditGroup.class })
    private String receiverAddressText;

    /**
     * 实际发货时间
     */
    private Date shippingTime;

    /**
     * 实际签收时间
     */
    private Date receiveTime;


}
