package org.dromara.pm.domain.vo;

import org.dromara.pm.domain.PromoGoodsSku;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 活动商品配置视图对象 pm_promo_goods_sku
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoGoodsSku.class)
public class PromoGoodsSkuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 关联营销活动ID
     */
    @ExcelProperty(value = "关联营销活动ID")
    private Long activityId;

    /**
     * 关联原生SPU ID
     */
    @ExcelProperty(value = "关联原生SPU ID")
    private Long spuId;

    /**
     * 关联原生SKU ID
     */
    @ExcelProperty(value = "关联原生SKU ID")
    private Long skuId;

    /**
     * 活动秒杀价/特价
     */
    @ExcelProperty(value = "活动秒杀价/特价")
    private BigDecimal promoPrice;

    /**
     * 秒杀库存量
     */
    @ExcelProperty(value = "秒杀库存量")
    private Long activityStock;

    /**
     * 已秒杀数量
     */
    @ExcelProperty(value = "已秒杀数量")
    private Long soldStock;

    /**
     * 该SKU单人限购件数
     */
    @ExcelProperty(value = "该SKU单人限购件数")
    private Long userLimitQty;

    /**
     * 乐观锁版本号
     */
    @ExcelProperty(value = "乐观锁版本号")
    private Integer version;


}
