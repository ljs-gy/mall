package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.AnnouncementItemGoods;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 包裹子商品业务对象 bid_announcement_item_goods
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AnnouncementItemGoods.class, reverseConvertGenerate = false)
public class AnnouncementItemGoodsBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotNull(message = "关联的公告标的条目ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联的公告标的条目ID
     */
    private Long itemId;

    @NotNull(message = "关联的SKU 资源ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联的SKU 资源ID
     */
    private Long subResourceId;

    @NotBlank(message = "子商品单项名称不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 子商品单项名称
     */
    private String goodsName;

    /**
     * 子商品牌号/技术规格快照
     */
    private String brandSpec;

    @NotNull(message = "单项在包裹里放入的具体重量/数量不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 单项在包裹里放入的具体重量/数量
     */
    private BigDecimal goodsSupplyWeight;

    @NotNull(message = "该单项资源的底价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 该单项资源的底价
     */
    private BigDecimal pricesetNPrice;

}