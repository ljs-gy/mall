package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.SpecGroup;
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
 * 商品规格组视图对象 goods_spec_group
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpecGroup.class)
public class SpecGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格组主键ID
     */
    @ExcelProperty(value = "规格组主键ID")
    private Long id;

    /**
     * 关联商品类型ID
     */
    @ExcelProperty(value = "关联商品类型ID")
    private Long typeId;

    /**
     * 规格组名称
     */
    @ExcelProperty(value = "规格组名称")
    private String specGroupName;


}
