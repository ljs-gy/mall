package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Parameter;
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
 * 商品参数视图对象 goods_parameter
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Parameter.class)
public class ParameterVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数主键ID
     */
    @ExcelProperty(value = "参数主键ID")
    private Long id;

    /**
     * 关联商品类型ID
     */
    @ExcelProperty(value = "关联商品类型ID")
    private Long typeId;

    /**
     * 参数名称
     */
    @ExcelProperty(value = "参数名称")
    private String parameterName;


}
