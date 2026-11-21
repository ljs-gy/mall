package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 营销活动阶梯规则对象 pm_promo_tier_rule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_promo_tier_rule")
public class PromoTierRule extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联营销活动ID
     */
    private Long activityId;

    /**
     * 门槛: 满X元 或 满X件
     */
    private BigDecimal thresholdValue;

    /**
     * 优惠强度: 减X元 或 打X折(0.85)
     */
    private BigDecimal benefitValue;

    /**
     * 满赠挂载的赠品（非表字段，仅 Redis 缓存/结算组装用）
     */
    @TableField(exist = false)
    private List<PromoTierGift> giftList;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;


}
