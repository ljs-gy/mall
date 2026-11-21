package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 营销活动与优惠券人群关系对象 pm_promo_scope_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_promo_scope_user")
public class PromoScopeUser extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /**
     * 来源主体类型: 1营销活动, 2优惠券, 3满赠券发放规则
     */
    private Long sourceType;

    private Long sourceId;

    /**
     * 目标ID（用户ID或会员等级ID，类型由主表 user_scope_type 决定）
     */
    private Long targetId;

    @TableLogic
    private String delFlag;

}
