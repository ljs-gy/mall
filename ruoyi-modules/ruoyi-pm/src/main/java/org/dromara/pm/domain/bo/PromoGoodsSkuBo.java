package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 活动商品配置业务对象 pm_promo_goods_sku
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PromoGoodsSku.class, reverseConvertGenerate = false)
public class PromoGoodsSkuBo extends BaseEntity {

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
     * 关联原生SPU ID
     */
    @NotNull(message = "关联原生SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /**
     * 关联原生SKU ID
     */
    @NotNull(message = "关联原生SKU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long skuId;

    /**
     * 活动秒杀价/特价
     */
    @NotNull(message = "活动秒杀价/特价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal promoPrice;

    /**
     * 秒杀库存量
     */
    @NotNull(message = "秒杀库存量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long activityStock;

    /**
     * 已秒杀数量
     */
    @NotNull(message = "已秒杀数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long soldStock;

    /**
     * 该SKU单人限购件数
     */
    @NotNull(message = "该SKU单人限购件数不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userLimitQty;

    /**
     * 乐观锁版本号
     */
    private Integer version;


}
