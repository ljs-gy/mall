package org.dromara.dis.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.dis.domain.DistRelation;
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
 * 分销上下级关系绑定视图对象 dist_relation
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistRelation.class)
public class DistRelationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 当前用户id
     */
    @ExcelProperty(value = "当前用户id")
    private Long userinfoId;

    /**
     * 上级分销商的用户id
     */
    @ExcelProperty(value = "上级分销商的用户id")
    private Long parentUserinfoId;

    /**
     * 绑定关系时间
     */
    @ExcelProperty(value = "绑定关系时间")
    private Date bindTime;


}
