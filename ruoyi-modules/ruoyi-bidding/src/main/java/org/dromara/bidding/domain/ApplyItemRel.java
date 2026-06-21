package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 报名标的关联对象 bid_apply_item_rel
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_apply_item_rel")
public class ApplyItemRel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 外键关联报名记录主表ID
     */
    private Long applyId;

    /**
     * 买家勾选要出价的具体的条目 ID
     */
    private Long itemId;

    /**
     * 该商品冻结保证金金额
     */
    private BigDecimal depositAmount;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}