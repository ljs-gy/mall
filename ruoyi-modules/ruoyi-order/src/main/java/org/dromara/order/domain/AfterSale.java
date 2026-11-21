package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 售后退货单对象 oc_after_sale
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_after_sale")
public class AfterSale extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 售后单主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 售后单号
     */
    private String afterSaleNo;

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
     * 归属商家/物权编码
     */
    private String memberCode;

    /**
     * 服务类型：1仅退款(待发货)，2退货退款(待收货)
     */
    private Long serviceType;

    /**
     * 发起来源：1买家申请，2商家主动退单，3运营平台退单
     */
    private Long applySource;

    /**
     * 售后状态: 10待审核, 20待买家退货(仅退货退款), 30待商家验货(已寄回),
     * 40退款中(验货通过/仅退款审核通过), 50退款完成,
     * 90拒绝(审核拒绝/验货不通过), 99已撤销
     */
    private Long status;

    /**
     * 本次申请退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String applyReason;

    /**
     * 买家上传凭证图片
     */
    private String proofImgs;

    /**
     * 买家申请说明
     */
    private String applyRemark;

    /**
     * 退货快递公司
     */
    private String returnExpressCompany;

    /**
     * 退货快递单号
     */
    private String returnExpressNo;

    /**
     * 买家寄回填写时间
     */
    private Date returnTime;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 卖家留言
     */
    private String auditRemark;

    /**
     * 商家验货完成时间
     */
    private Date inspectTime;

    /**
     * 验货备注
     */
    private String inspectRemark;

    /**
     * 退货收货人
     */
    private String receiverName;

    /**
     * 退货收件人手机
     */
    private String receiverMobile;

    /**
     * 退货详细地址
     */
    private String receiverAddress;

    /**
     * 成功退款时间
     */
    private Date refundTime;

    /**
     * 第三方支付退款流水号
     */
    private String refundTradeNo;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;


}
