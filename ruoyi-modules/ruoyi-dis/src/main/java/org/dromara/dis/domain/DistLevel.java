package org.dromara.dis.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 分销员等级对象 dist_level
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dist_level")
public class DistLevel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 自购金额满多少元
     */
    private BigDecimal upgradeSelfAmount;

    /**
     * 推广客户满多少人
     */
    private Long upgradeInviteCount;

    /**
     * 累计佣金满多少元
     */
    private BigDecimal upgradeTotalCommission;

    /**
     * 等级权重
     */
    private Long levelWeight;

    /**
     * 一级分佣比例
     */
    private BigDecimal firstRate;

    /**
     * 二级分佣比例
     */
    private BigDecimal secondRate;

    /**
     * 三级分佣比例
     */
    private BigDecimal thirdRate;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
