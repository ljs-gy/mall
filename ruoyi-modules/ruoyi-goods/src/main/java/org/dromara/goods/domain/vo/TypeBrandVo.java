package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.TypeBrand;
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
 * 商品类型与品牌关系视图对象 goods_type_brand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TypeBrand.class)
public class TypeBrandVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 商品类型ID
     */
    @ExcelProperty(value = "商品类型ID")
    private Long typeId;

    /**
     * 品牌ID
     */
    @ExcelProperty(value = "品牌ID")
    private Long brandId;


}
