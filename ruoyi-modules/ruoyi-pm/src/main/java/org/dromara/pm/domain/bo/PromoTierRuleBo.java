package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.PromoTierRule;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 营销活动阶梯规则业务对象 pm_promo_tier_rule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PromoTierRule.class, reverseConvertGenerate = false)
public class PromoTierRuleBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联营销活动ID
     */
    @NotNull(message = "关联营销活动ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long activityId;

    /**
     * 门槛: 满X元 或 满X件
     */
    @NotNull(message = "门槛: 满X元 或 满X件不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal thresholdValue;

    /**
     * 优惠强度: 减X元 或 打X折(0.85)
     */
    @NotNull(message = "优惠强度: 减X元 或 打X折(0.85)不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal benefitValue;

}
