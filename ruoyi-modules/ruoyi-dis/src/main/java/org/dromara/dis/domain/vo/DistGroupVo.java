package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.DistGroup;
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
 * 分销员分组视图对象 dist_group
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistGroup.class)
public class DistGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 分组名称
     */
    @ExcelProperty(value = "分组名称")
    private String groupName;


}
