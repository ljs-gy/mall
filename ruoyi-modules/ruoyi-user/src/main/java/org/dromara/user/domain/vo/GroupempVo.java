package org.dromara.user.domain.vo;

import org.dromara.user.domain.Groupemp;
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
 * 群组员工职位关系视图对象 um_groupemp
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Groupemp.class)
public class GroupempVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 员工编码
     */
    @ExcelProperty(value = "员工编码")
    private String employeeCode;

    /**
     * 群组ID
     */
    @ExcelProperty(value = "群组ID")
    private Long groupId;

    /**
     * 在该群组里担任的职位编码
     */
    @ExcelProperty(value = "在该群组里担任的职位编码")
    private String groupPositionCode;


}
