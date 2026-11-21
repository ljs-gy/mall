package org.dromara.order.domain.vo;

import org.dromara.order.domain.AfterSale;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 售后退货单视图对象 oc_after_sale
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AfterSale.class)
public class AfterSaleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "售后单主键ID")
    private Long id;

    @ExcelProperty(value = "售后单号")
    private String afterSaleNo;

    @ExcelProperty(value = "关联店铺子订单号")
    private String orderNo;

    @ExcelProperty(value = "买家ID")
    private Long userinfoId;

    @ExcelProperty(value = "买家业务编码")
    private String memberBcode;

    @ExcelProperty(value = "物权编码")
    private String memberCode;

    @ExcelProperty(value = "服务类型")
    private Long serviceType;

    @ExcelProperty(value = "发起来源")
    private Long applySource;

    @ExcelProperty(value = "售后状态")
    private Long status;

    @ExcelProperty(value = "本次申请退款金额")
    private BigDecimal refundAmount;

    @ExcelProperty(value = "退款原因")
    private String applyReason;

    @ExcelProperty(value = "凭证图片")
    private String proofImgs;

    @ExcelProperty(value = "买家申请说明")
    private String applyRemark;

    @ExcelProperty(value = "退货快递公司")
    private String returnExpressCompany;

    @ExcelProperty(value = "退货快递单号")
    private String returnExpressNo;

    @ExcelProperty(value = "买家寄回填写时间")
    private Date returnTime;

    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    @ExcelProperty(value = "审核人")
    private String auditBy;

    @ExcelProperty(value = "卖家留言")
    private String auditRemark;

    @ExcelProperty(value = "商家验货完成时间")
    private Date inspectTime;

    @ExcelProperty(value = "验货备注")
    private String inspectRemark;

    @ExcelProperty(value = "退货收货人")
    private String receiverName;

    @ExcelProperty(value = "退货收件人手机")
    private String receiverMobile;

    @ExcelProperty(value = "退货详细地址")
    private String receiverAddress;

    @ExcelProperty(value = "成功退款时间")
    private Date refundTime;

    @ExcelProperty(value = "第三方退款流水号")
    private String refundTradeNo;


}
