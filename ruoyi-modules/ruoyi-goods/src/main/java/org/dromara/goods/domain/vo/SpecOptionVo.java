package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.SpecOption;
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
 * 商品规格选项视图对象 goods_spec_option
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SpecOption.class)
public class SpecOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 选项主键ID
     */
    @ExcelProperty(value = "选项主键ID")
    private Long id;

    /**
     * 关联规格ID
     */
    @ExcelProperty(value = "关联规格ID")
    private Long specId;

    /**
     * 规格选项名称
     */
    @ExcelProperty(value = "规格选项名称")
    private String specOptionName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
