package org.dromara.dis.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 价格规则适用范围明细项
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DisPriceRuleScopeItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 1商品 2分类 3品牌 4门店 */
    @NotNull(message = "适用范围类型不能为空")
    private Long scopeType;

    @NotBlank(message = "关联值不能为空")
    private String scopeValue;
}
