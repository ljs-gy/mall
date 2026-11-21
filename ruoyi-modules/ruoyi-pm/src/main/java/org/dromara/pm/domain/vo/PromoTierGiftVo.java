package org.dromara.pm.domain.vo;

import org.dromara.pm.domain.PromoTierGift;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 营销活动赠品明细视图对象
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoTierGift.class)
public class PromoTierGiftVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "活动ID")
    private Long activityId;

    @ExcelProperty(value = "阶梯规则ID")
    private Long tierRuleId;

    @ExcelProperty(value = "赠品SKU ID")
    private Long giftSkuId;

    @ExcelProperty(value = "赠送数量")
    private Long giftCount;

    @ExcelProperty(value = "排序")
    private Long sort;

}
