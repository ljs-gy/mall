package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.AnnouncementItemGoods;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 包裹子商品视图对象 bid_announcement_item_goods
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AnnouncementItemGoods.class)
public class AnnouncementItemGoodsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联的公告标的条目ID
     */
    @ExcelProperty(value = "关联的公告标的条目ID")
    private Long itemId;

    /**
     * 关联的SKU 资源ID
     */
    @ExcelProperty(value = "关联的SKU 资源ID")
    private Long subResourceId;

    /**
     * 子商品单项名称
     */
    @ExcelProperty(value = "子商品单项名称")
    private String goodsName;

    /**
     * 子商品牌号/技术规格快照
     */
    @ExcelProperty(value = "子商品牌号/技术规格快照")
    private String brandSpec;

    /**
     * 单项在包裹里放入的具体重量/数量
     */
    @ExcelProperty(value = "单项在包裹里放入的具体重量/数量")
    private BigDecimal goodsSupplyWeight;

    /**
     * 该单项资源的底价
     */
    @ExcelProperty(value = "该单项资源的底价")
    private BigDecimal pricesetNPrice;

}