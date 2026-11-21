package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.DistLevel;
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
 * 分销员等级视图对象 dist_level
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistLevel.class)
public class DistLevelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 等级名称
     */
    @ExcelProperty(value = "等级名称")
    private String levelName;

    /**
     * 自购金额满多少元
     */
    @ExcelProperty(value = "自购金额满多少元")
    private java.math.BigDecimal upgradeSelfAmount;

    /**
     * 推广客户满多少人
     */
    @ExcelProperty(value = "推广客户满多少人")
    private Long upgradeInviteCount;

    /**
     * 累计佣金满多少元
     */
    @ExcelProperty(value = "累计佣金满多少元")
    private java.math.BigDecimal upgradeTotalCommission;

    /**
     * 等级权重
     */
    @ExcelProperty(value = "等级权重")
    private Long levelWeight;

    /**
     * 一级分佣比例
     */
    @ExcelProperty(value = "一级分佣比例")
    private java.math.BigDecimal firstRate;

    /**
     * 二级分佣比例
     */
    @ExcelProperty(value = "二级分佣比例")
    private java.math.BigDecimal secondRate;

    /**
     * 三级分佣比例
     */
    @ExcelProperty(value = "三级分佣比例")
    private java.math.BigDecimal thirdRate;


}
