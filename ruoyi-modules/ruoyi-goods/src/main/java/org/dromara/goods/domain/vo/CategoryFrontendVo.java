package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.CategoryFrontend;
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
 * 前台商品分类视图对象 goods_category_frontend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CategoryFrontend.class)
public class CategoryFrontendVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 前台分类主键ID
     */
    @ExcelProperty(value = "前台分类主键ID")
    private Long id;

    /**
     * 前台分类名称
     */
    @ExcelProperty(value = "前台分类名称")
    private String frontCategoryName;

    /**
     * 父分类ID，0表示一级分类
     */
    @ExcelProperty(value = "父分类ID，0表示一级分类")
    private Long parentId;

    /**
     * 前台分类图片（仅三级有效）
     */
    @ExcelProperty(value = "前台分类图片", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "仅=三级有效")
    private String logoUrl;

    /**
     * 是否隐藏（0否，1是）
     */
    @ExcelProperty(value = "是否隐藏", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=否，1是")
    private Long isHidden;


}
