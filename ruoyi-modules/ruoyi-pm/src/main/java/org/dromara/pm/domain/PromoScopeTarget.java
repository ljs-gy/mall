package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 营销范围对象 pm_promo_scope_target
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_promo_scope_target")
public class PromoScopeTarget extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /**
     * 来源主体类型: 1营销活动, 2优惠券
     */
    private Long sourceType;

    private Long sourceId;

    /**
     * 绑定目标分类: 1供应商, 2商品分类, 3品牌, 4sku
     */
    private Long targetCategory;

    /**
     * 目标标识：供应商=userinfoCode；分类/品牌/SKU=对应 ID 字符串
     */
    private String targetId;

    @TableLogic
    private String delFlag;

}
