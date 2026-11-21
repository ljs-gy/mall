package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.CategoryFrontBackRel;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前后台分类映射关系视图对象 goods_category_front_back_rel
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CategoryFrontBackRel.class)
public class CategoryFrontBackRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 前台类目ID
     */
    @ExcelProperty(value = "前台类目ID")
    private Long frontendCategoryId;

    /**
     * 后台类目ID
     */
    @ExcelProperty(value = "后台类目ID")
    private Long backendCategoryId;

}
