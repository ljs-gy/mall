package org.dromara.dis.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.dis.domain.PriceRule;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 渠道价格规则视图
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@AutoMapper(target = PriceRule.class)
public class PriceRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键")
    private Long id;

    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    @ExcelProperty(value = "规则名称")
    private String ruleName;

    @ExcelProperty(value = "价格同步规则")
    private Long dpriceSyncType;

    @ExcelProperty(value = "销售价规则值")
    private BigDecimal salesPriceRuleVal;

    @ExcelProperty(value = "市场价规则值")
    private BigDecimal marketPriceRuleVal;

    @ExcelProperty(value = "默认销售价")
    private BigDecimal defaultSalesPrice;

    @ExcelProperty(value = "默认市场价")
    private BigDecimal defaultMarketPrice;

    @ExcelProperty(value = "价格自动更新")
    private Long dpriceSync;

    @ExcelProperty(value = "价格精度")
    private Long dpriceVer;

    @ExcelProperty(value = "优先级")
    private Long dpriceWeight;

    @ExcelProperty(value = "生效开始")
    private Date dpriceSydate;

    @ExcelProperty(value = "生效结束")
    private Date dpriceEydate;

    /** 适用范围（详情回显；空=全局） */
    private List<PriceRuleScopeVo> scopeItems;
}
