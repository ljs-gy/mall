package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 营销活动赠品明细对象 pm_promo_tier_gift
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_promo_tier_gift")
public class PromoTierGift extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /**
     * 关联营销活动ID
     */
    private Long activityId;

    /**
     * 关联阶梯门槛规则ID
     */
    private Long tierRuleId;

    /**
     * 赠品SKU ID
     */
    private Long giftSkuId;

    /**
     * 赠送数量
     */
    private Long giftCount;

    /**
     * 排序
     */
    private Long sort;

    @TableLogic
    private String delFlag;

}
