package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Type;
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
 * 商品类型视图对象 goods_type
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Type.class)
public class TypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 类型主键ID
     */
    @ExcelProperty(value = "类型主键ID")
    private Long id;

    /**
     * 类型名称
     */
    @ExcelProperty(value = "类型名称")
    private String typeName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
