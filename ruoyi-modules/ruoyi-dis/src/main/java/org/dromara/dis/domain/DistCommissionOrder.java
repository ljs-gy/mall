package org.dromara.dis.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 分佣业务头表 dist_commission_order
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dist_commission_order")
public class DistCommissionOrder extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 渠道编码 */
    private String channelCode;

    /** 分佣单号 */
    private String commissionNo;

    /** 业务订单号 */
    private String orderNo;

    /** 下单人 userinfoId */
    private Long buyerUserinfoId;

    /** 计佣基数金额 */
    private BigDecimal orderAmount;

    /** 备注 */
    private String remark;

    @TableLogic
    private String delFlag;
}
