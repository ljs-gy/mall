package org.dromara.pm.domain.vo;

import org.dromara.pm.domain.PromoScopeTarget;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoScopeTarget.class)
public class PromoScopeTargetVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "来源主体类型")
    private Long sourceType;

    @ExcelProperty(value = "来源主体ID")
    private Long sourceId;

    @ExcelProperty(value = "目标分类:1供应商2分类3品牌4sku")
    private Long targetCategory;

    @ExcelProperty(value = "目标标识(供应商=userinfoCode)")
    private String targetId;

}
