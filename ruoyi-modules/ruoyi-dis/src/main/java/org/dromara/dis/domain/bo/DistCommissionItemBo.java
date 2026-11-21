package org.dromara.dis.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.dis.domain.DistCommissionItem;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 分佣明细业务对象 dist_commission_item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistCommissionItem.class, reverseConvertGenerate = false)
public class DistCommissionItemBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    @NotNull(message = "分佣头表ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long commissionOrderId;

    @NotBlank(message = "分佣单号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String commissionNo;

    @NotBlank(message = "订单号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String orderNo;

    @NotNull(message = "受益人不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long beneficiaryUserinfoId;

    @NotNull(message = "分佣层级不能为空", groups = {AddGroup.class, EditGroup.class})
    private Integer level;

    private BigDecimal rate;

    @NotNull(message = "佣金金额不能为空", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal amount;

    /** 状态：0待结算 1已结算 2已取消 */
    private Integer status;

    private Date settleTime;

    private Date cancelTime;

    private String cancelReason;

    private String accountJournalNo;
}
