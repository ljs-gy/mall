package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.PriceRuleScope;
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
 * 价格规则适用范围视图对象 dis_price_rule_scope
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PriceRuleScope.class)
public class PriceRuleScopeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联价格规则ID
     */
    @ExcelProperty(value = "关联价格规则ID")
    private Long priceRuleId;

    /**
     * 适用类型：1按商品，2按分类，3按品牌，4按门店
     */
    @ExcelProperty(value = "适用类型：1按商品，2按分类，3按品牌，4按门店")
    private Long scopeType;

    /**
     * 对应关联值
     */
    @ExcelProperty(value = "对应关联值")
    private String scopeValue;


}
