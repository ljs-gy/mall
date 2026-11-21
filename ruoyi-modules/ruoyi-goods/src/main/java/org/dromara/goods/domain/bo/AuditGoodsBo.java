package org.dromara.goods.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class AuditGoodsBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "SPU ID不能为空")
    private Long spuId;


    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    private String rejectReason;
}
