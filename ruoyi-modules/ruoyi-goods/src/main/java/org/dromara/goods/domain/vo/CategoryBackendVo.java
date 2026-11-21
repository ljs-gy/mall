package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.CategoryBackend;
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
 * 后台商品分类视图对象 goods_category_backend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CategoryBackend.class)
public class CategoryBackendVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类主键ID
     */
    @ExcelProperty(value = "分类主键ID")
    private Long id;

    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    private String categoryName;

    /**
     * 父分类ID，0表示一级分类
     */
    @ExcelProperty(value = "父分类ID，0表示一级分类")
    private Long parentId;

    /**
     * 关联商品类型ID（仅三级分类有效）
     */
    @ExcelProperty(value = "关联商品类型ID", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "仅=三级分类有效")
    private Long typeId;

    /**
     * 分类logo（仅三级分类有效）
     */
    @ExcelProperty(value = "分类logo", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "仅=三级分类有效")
    private String logoUrl;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
