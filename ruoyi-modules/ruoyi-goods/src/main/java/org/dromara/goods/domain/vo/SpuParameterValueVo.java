package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.SpuParameterValue;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品SPU参数值视图对象 goods_spu_parameter_value
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpuParameterValue.class)
public class SpuParameterValueVo implements Serializable {

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
     * 关联参数ID
     */
    @ExcelProperty(value = "关联参数ID")
    private Long parameterId;

    /**
     * 手动填写的参数值内容
     */
    @ExcelProperty(value = "参数值")
    private String parameterValueText;

}
