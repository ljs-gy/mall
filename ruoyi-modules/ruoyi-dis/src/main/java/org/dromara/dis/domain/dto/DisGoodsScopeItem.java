package org.dromara.dis.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道商品选品明细项
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DisGoodsScopeItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "维度类型不能为空")
    private String scopeType;


    @NotBlank(message = "运算符不能为空")
    private String scopeTerm;

    @NotBlank(message = "目标值不能为空")
    private String scopeValue;

    private String ruleValueName;
}
