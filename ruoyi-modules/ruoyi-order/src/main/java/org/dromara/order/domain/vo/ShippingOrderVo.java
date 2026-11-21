package org.dromara.order.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.order.domain.ShippingOrder;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 发货单视图对象 oc_shipping_order
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ShippingOrder.class)
public class ShippingOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 发货单主键ID
     */
    @ExcelProperty(value = "发货单主键ID")
    private Long id;

    /**
     * 发货单号
     */
    @ExcelProperty(value = "发货单号")
    private String shippingNo;

    /**
     * 关联店铺子订单号
     */
    @ExcelProperty(value = "关联店铺子订单号")
    private String orderNo;

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
     * 物权编码
     */
    @ExcelProperty(value = "物权编码")
    private String memberCode;

    /**
     * 发货状态：10待发货，20已发货，30已签收
     */
    @ExcelProperty(value = "发货状态：10待发货，20已发货，30已签收")
    private Long shippingStatus;

    /**
     * 快递公司
     */
    @ExcelProperty(value = "快递公司")
    private String expressCompany;

    /**
     * 快递单号
     */
    @ExcelProperty(value = "快递单号")
    private String expressNo;

    /**
     * 收货人姓名
     */
    @ExcelProperty(value = "收货人姓名")
    private String consigneeName;

    /**
     * 收货人手机号
     */
    @ExcelProperty(value = "收货人手机号")
    private String consigneeMobile;

    /**
     * 收货地址快照
     */
    @ExcelProperty(value = "收货地址快照")
    private String receiverAddressText;

    /**
     * 实际发货时间
     */
    @ExcelProperty(value = "实际发货时间")
    private Date shippingTime;

    /**
     * 实际签收时间
     */
    @ExcelProperty(value = "实际签收时间")
    private Date receiveTime;


}
