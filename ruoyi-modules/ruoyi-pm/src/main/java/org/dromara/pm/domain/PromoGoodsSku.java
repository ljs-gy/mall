package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 活动商品配置对象 pm_promo_goods_sku
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_promo_goods_sku")
public class PromoGoodsSku extends TenantEntity {

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
     * 关联原生SPU ID
     */
    private Long spuId;

    /**
     * 关联原生SKU ID
     */
    private Long skuId;

    /**
     * 活动秒杀价/特价
     */
    private BigDecimal promoPrice;

    /**
     * 秒杀活动配额总库存（-1 不限；在线预占走 Redis）
     */
    private Long activityStock;

    /**
     * 已支付售出数量
     */
    private Long soldStock;

    /**
     * 该SKU单人限购件数
     */
    private Long userLimitQty;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;


}
