package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 包裹子商品对象 bid_announcement_item_goods
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_announcement_item_goods")
public class AnnouncementItemGoods extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联的公告标的条目ID
     */
    private Long itemId;

    /**
     * 关联的SKU 资源ID
     */
    private Long subResourceId;

    /**
     * 子商品单项名称
     */
    private String goodsName;

    /**
     * 子商品牌号/技术规格快照
     */
    private String brandSpec;

    /**
     * 单项在包裹里放入的具体重量/数量
     */
    private BigDecimal goodsSupplyWeight;

    /**
     * 该单项资源的底价
     */
    private BigDecimal pricesetNPrice;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}