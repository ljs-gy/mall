package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.AttributeOption;
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
 * 商品属性选项视图对象 goods_attribute_option
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AttributeOption.class)
public class AttributeOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 选项主键ID
     */
    @ExcelProperty(value = "选项主键ID")
    private Long id;

    /**
     * 关联属性ID
     */
    @ExcelProperty(value = "关联属性ID")
    private Long attributeId;

    /**
     * 属性选项名称
     */
    @ExcelProperty(value = "属性选项名称")
    private String attributeOptionName;


}
