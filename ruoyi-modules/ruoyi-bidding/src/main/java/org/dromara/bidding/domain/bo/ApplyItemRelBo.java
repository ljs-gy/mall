package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.ApplyItemRel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 报名标的关联业务对象 bid_apply_item_rel
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ApplyItemRel.class, reverseConvertGenerate = false)
public class ApplyItemRelBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotNull(message = "外键关联报名记录主表ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 外键关联报名记录主表ID
     */
    private Long applyId;

    @NotNull(message = "买家勾选要出价的具体的条目 ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 买家勾选要出价的具体的条目 ID
     */
    private Long itemId;

    @NotNull(message = "该商品冻结保证金金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 该商品冻结保证金金额
     */
    private BigDecimal depositAmount;

}