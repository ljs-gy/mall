package org.dromara.pm.domain.vo;

import org.dromara.pm.domain.PromoScopeUser;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoScopeUser.class)
public class PromoScopeUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "来源主体类型")
    private Long sourceType;

    @ExcelProperty(value = "来源主体ID")
    private Long sourceId;

    @ExcelProperty(value = "目标ID")
    private Long targetId;

}
