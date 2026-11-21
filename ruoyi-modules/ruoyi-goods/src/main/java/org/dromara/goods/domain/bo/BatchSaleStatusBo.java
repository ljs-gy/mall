package org.dromara.goods.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量上下架
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
public class BatchSaleStatusBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "请选择要操作的商品")
    private List<Long> spuIds;

    /** 0下架 1上架 */
    @NotNull(message = "上下架状态不能为空")
    private Long saleStatus;
}
