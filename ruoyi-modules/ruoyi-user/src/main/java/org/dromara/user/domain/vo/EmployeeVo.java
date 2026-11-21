package org.dromara.user.domain.vo;

import org.dromara.user.domain.Employee;
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
 * 员工视图对象 um_employee
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Employee.class)
public class EmployeeVo implements Serializable {

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
     * 关联用户编码
     */
    @ExcelProperty(value = "关联用户编码")
    private String userCode;

    /**
     * 员工所属组织编码
     */
    @ExcelProperty(value = "员工所属组织编码")
    private String orgCode;

    /**
     * 员工姓名
     */
    @ExcelProperty(value = "员工姓名")
    private String employeeName;

    /**
     * 手机号
     */
    @ExcelProperty(value = "手机号")
    private String phone;

    /**
     * 公司职位编码
     */
    @ExcelProperty(value = "公司职位编码")
    private String companyPositionCode;

    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;


}
