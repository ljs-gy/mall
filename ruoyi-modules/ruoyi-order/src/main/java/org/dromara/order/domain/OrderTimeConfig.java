package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 订单交易时效配置对象 oc_order_time_config
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_order_time_config")
public class OrderTimeConfig extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 未支付超时取消时间(分钟)，默认30分钟
     */
    private Long unpaidCancelMinutes;

    /**
     * 发货后自动确认收货时间(天)，默认7天
     */
    private Long autoReceiveDays;

    /**
     * 确认收货后自动交易完成时间(天)，默认7天
     */
    private Long autoCompleteDays;

    /**
     * 状态：0停用，1启用
     */
    private Long status;

    /**
     * 删除标志：0存在，1删除
     */
    @TableLogic
    private String delFlag;


}
