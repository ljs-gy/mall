package org.dromara.order.domain.bo;

import org.dromara.order.domain.OrderTimeConfig;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 订单交易时效配置业务对象 oc_order_time_config
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OrderTimeConfig.class, reverseConvertGenerate = false)
public class OrderTimeConfigBo extends BaseEntity {

    /**
     * 配置主键ID
     */
    @NotNull(message = "配置主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 渠道编码
     */
    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /**
     * 未支付超时取消时间(分钟)，默认30分钟
     */
    @NotNull(message = "未支付超时取消时间(分钟)，默认30分钟不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long unpaidCancelMinutes;

    /**
     * 发货后自动确认收货时间(天)，默认7天
     */
    @NotNull(message = "发货后自动确认收货时间(天)，默认7天不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long autoReceiveDays;

    /**
     * 确认收货后自动交易完成时间(天)，默认7天
     */
    @NotNull(message = "确认收货后自动交易完成时间(天)，默认7天不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long autoCompleteDays;

    /**
     * 状态：0停用，1启用
     */
    @NotNull(message = "状态：0停用，1启用不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;


}
