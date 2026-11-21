package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.SpuAttributeValue;
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
 * 商品SPU属性值视图对象 goods_spu_attribute_value
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpuAttributeValue.class)
public class SpuAttributeValueVo implements Serializable {

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
     * 关联属性ID
     */
    @ExcelProperty(value = "关联属性ID")
    private Long attributeId;

    /**
     * 关联属性选项ID
     */
    @ExcelProperty(value = "关联属性选项ID")
    private Long attributeOptionId;


}
