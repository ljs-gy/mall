package org.dromara.dis.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.dis.domain.DistCommissionOrder;

import java.math.BigDecimal;

/**
 * 分佣业务头表业务对象 dist_commission_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistCommissionOrder.class, reverseConvertGenerate = false)
public class DistCommissionOrderBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    private String channelCode;

    @NotBlank(message = "分佣单号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String commissionNo;

    @NotBlank(message = "订单号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String orderNo;

    @NotNull(message = "下单人不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long buyerUserinfoId;

    @NotNull(message = "计佣金额不能为空", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal orderAmount;

    private String remark;
}
