package org.dromara.pm.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.pm.domain.PromoMutexGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 营销活动互斥组视图
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoMutexGroup.class)
public class PromoMutexGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键")
    private Long id;

    @ExcelProperty(value = "互斥组编码")
    private String groupCode;

    @ExcelProperty(value = "互斥组名称")
    private String groupName;

    @ExcelProperty(value = "活动类型")
    private String promoType;

    /**
     * 1同组互斥择优, 2绝对超级排他
     */
    @ExcelProperty(value = "互斥规则")
    private Long mutexRule;

    @ExcelProperty(value = "优先级")
    private Long priority;

    @ExcelProperty(value = "状态")
    private Long status;

    private Date createTime;

    private Date updateTime;

}
