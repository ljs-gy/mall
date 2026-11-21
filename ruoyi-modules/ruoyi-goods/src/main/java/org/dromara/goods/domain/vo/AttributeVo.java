package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Attribute;
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
 * 商品属性视图对象 goods_attribute
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Attribute.class)
public class AttributeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 属性主键ID
     */
    @ExcelProperty(value = "属性主键ID")
    private Long id;

    /**
     * 关联商品类型ID
     */
    @ExcelProperty(value = "关联商品类型ID")
    private Long typeId;

    /**
     * 属性名称
     */
    @ExcelProperty(value = "属性名称")
    private String attributeName;


}
