package org.dromara.user.domain.vo;

import org.dromara.user.domain.Department;
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
 * 部门视图对象 um_department
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Department.class)
public class DepartmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 所属组织代码
     */
    @ExcelProperty(value = "所属组织代码")
    private String orgCode;

    /**
     * 部门名称
     */
    @ExcelProperty(value = "部门名称")
    private String deptName;


}
