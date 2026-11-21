package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.DistUserGroup;
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
 * 分销员与分组关系视图对象 dist_user_group
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistUserGroup.class)
public class DistUserGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 用户信息ID
     */
    @ExcelProperty(value = "用户信息ID")
    private Long userinfoId;

    /**
     * 分组ID
     */
    @ExcelProperty(value = "分组ID")
    private Long groupId;


}
