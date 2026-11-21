package org.dromara.goods.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品上下架业务对象
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
public class SaleStatusBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "SPU ID不能为空")
    private Long spuId;

    /**
     * 上架状态：0下架，1上架
     */
    @NotNull(message = "上下架状态不能为空")
    private Long saleStatus;
}
