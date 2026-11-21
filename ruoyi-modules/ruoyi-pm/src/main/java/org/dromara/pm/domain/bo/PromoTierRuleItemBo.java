package org.dromara.pm.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.math.BigDecimal;
import java.util.List;

/**
 * 促销阶梯规则入参
 */
@Data
public class PromoTierRuleItemBo {

    @NotNull(message = "门槛值不能为空", groups = { AddGroup.class })
    @DecimalMin(value = "0.01", message = "门槛值必须大于0", groups = { AddGroup.class })
    private BigDecimal thresholdValue;

    private BigDecimal benefitValue;

    @Valid
    private List<PromoGiftItemBo> giftList;
}
