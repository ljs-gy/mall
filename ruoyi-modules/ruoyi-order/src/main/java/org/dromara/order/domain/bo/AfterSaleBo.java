package org.dromara.order.domain.bo;

import org.dromara.order.domain.AfterSale;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 售后退货单业务对象 oc_after_sale
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AfterSale.class, reverseConvertGenerate = false)
public class AfterSaleBo extends BaseEntity {

    @NotNull(message = "售后单主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "售后单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String afterSaleNo;

    @NotBlank(message = "关联店铺子订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    @NotNull(message = "买家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    private String memberBcode;

    private String memberCode;

    @NotNull(message = "服务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long serviceType;

    @NotNull(message = "发起来源不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long applySource;

    @NotNull(message = "售后状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

    @NotNull(message = "本次申请退款金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal refundAmount;

    @NotBlank(message = "退款原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applyReason;

    private String proofImgs;

    private String applyRemark;

    private String returnExpressCompany;

    private String returnExpressNo;

    private Date returnTime;

    private Date auditTime;

    private String auditBy;

    private String auditRemark;

    private Date inspectTime;

    private String inspectRemark;

    private String receiverName;

    private String receiverMobile;

    private String receiverAddress;

    private Date refundTime;

    private String refundTradeNo;


}
