package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Spec;
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
 * 商品规格视图对象 goods_spec
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Spec.class)
public class SpecVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格主键ID
     */
    @ExcelProperty(value = "规格主键ID")
    private Long id;

    /**
     * 关联规格组ID
     */
    @ExcelProperty(value = "关联规格组ID")
    private Long specGroupId;

    /**
     * 规格名称（如：颜色、尺码）
     */
    @ExcelProperty(value = "规格名称", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "如=：颜色、尺码")
    private String specName;


}
