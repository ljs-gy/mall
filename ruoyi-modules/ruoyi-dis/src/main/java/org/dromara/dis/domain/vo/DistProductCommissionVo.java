package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.DistProductCommission;
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
 * 商品分佣配置视图对象 dist_product_commission
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistProductCommission.class)
public class DistProductCommissionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 商品SPU ID
     */
    @ExcelProperty(value = "商品SPU ID")
    private Long spuId;

    /**
     * 分销员等级ID
     */
    @ExcelProperty(value = "分销员等级ID")
    private Long levelId;

    /**
     * 一级分佣比例
     */
    @ExcelProperty(value = "一级分佣比例")
    private Long firstRate;

    /**
     * 二级分佣比例
     */
    @ExcelProperty(value = "二级分佣比例")
    private Long secondRate;

    /**
     * 三级分佣比例
     */
    @ExcelProperty(value = "三级分佣比例")
    private Long thirdRate;


}
