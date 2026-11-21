package org.dromara.user.domain.vo;

import org.dromara.user.domain.Position;
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
 * 职位视图对象 um_position
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Position.class)
public class PositionVo implements Serializable {

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
     * 挂载关系类型：1挂载在组织节点，2挂载在部门，3挂载在群组
     */
    @ExcelProperty(value = "挂载关系类型：1挂载在组织节点，2挂载在部门，3挂载在群组")
    private Long relationType;

    /**
     * 关联的具体实体主键ID
     */
    @ExcelProperty(value = "关联的具体实体主键ID")
    private Long relationId;

    /**
     * 职位编码
     */
    @ExcelProperty(value = "职位编码")
    private String positionCode;

    /**
     * 职位名称
     */
    @ExcelProperty(value = "职位名称")
    private String positionName;


}
