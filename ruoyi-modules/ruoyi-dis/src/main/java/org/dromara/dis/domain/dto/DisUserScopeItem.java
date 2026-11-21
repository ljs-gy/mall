package org.dromara.dis.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道用户范围明细项
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DisUserScopeItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** ORG / USER / PROVINCE / CITY / DISTRICT */
    @NotBlank(message = "维度类型不能为空")
    private String buyType;

    /** IN / NOT_IN / = / != */
    @NotBlank(message = "运算符不能为空")
    private String buyTerm;

    @NotBlank(message = "匹配目标值不能为空")
    private String buyValue;

    private String ruleValueName;
}
