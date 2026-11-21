package org.dromara.dis.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.dis.domain.StockRule;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 渠道库存规则视图对象 dis_stock_rule
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = StockRule.class)
public class StockRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 关联渠道编码
     */
    @ExcelProperty(value = "关联渠道编码")
    private String channelCode;

    /**
     * 规则名称
     */
    @ExcelProperty(value = "规则名称")
    private String ruleName;

    /**
     * 库存同步规则：1按百分比，2按固定值
     */
    @ExcelProperty(value = "库存同步规则：1按百分比，2按固定值")
    private Long dgnumPro;

    /**
     * 库存数值/百分比例值
     */
    @ExcelProperty(value = "库存数值/百分比例值")
    private BigDecimal dgnumPrice;

    /**
     * 库存自动更新开关（0否，1是）
     */
    @ExcelProperty(value = "库存自动更新开关", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=否，1是")
    private Long dgnumSync;

    /**
     * 安全库存最低阈值
     */
    @ExcelProperty(value = "安全库存最低阈值")
    private Long dgnumMin;

    /**
     * 库存计算取整规则：1取整，2四舍五入到一位，3四舍五入到两位，0不处理
     */
    @ExcelProperty(value = "库存计算取整规则：1取整，2四舍五入到一位，3四舍五入到两位，0不处理")
    private Long dgnumVer;

    /**
     * 权重，越小越优先
     */
    @ExcelProperty(value = "权重，越小越优先")
    private Long dgnumWeight;

    /**
     * 生效时间
     */
    @ExcelProperty(value = "生效时间")
    private Date dgnumStydate;

    /**
     * 失效时间
     */
    @ExcelProperty(value = "失效时间")
    private Date dgnumEydate;


}
