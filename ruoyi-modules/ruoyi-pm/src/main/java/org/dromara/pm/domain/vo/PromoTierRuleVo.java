package org.dromara.pm.domain.vo;

import org.dromara.pm.domain.PromoTierRule;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 营销活动阶梯规则视图对象 pm_promo_tier_rule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoTierRule.class)
public class PromoTierRuleVo implements Serializable {

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
     * 门槛: 满X元 或 满X件
     */
    @ExcelProperty(value = "门槛: 满X元 或 满X件")
    private java.math.BigDecimal thresholdValue;

    /**
     * 优惠强度: 减X元 或 打X折(0.85)
     */
    @ExcelProperty(value = "优惠强度: 减X元 或 打X折(0.85)")
    private java.math.BigDecimal benefitValue;

}
