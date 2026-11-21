package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Brand;
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
 * 商品品牌视图对象 goods_brand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Brand.class)
public class BrandVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 品牌主键ID
     */
    @ExcelProperty(value = "品牌主键ID")
    private Long id;

    /**
     * 品牌名称
     */
    @ExcelProperty(value = "品牌名称")
    private String brandName;

    /**
     * 品牌LOGO
     */
    @ExcelProperty(value = "品牌LOGO")
    private String logoUrl;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
