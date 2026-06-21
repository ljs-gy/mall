package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.ApplyItemRel;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 报名标的关联视图对象 bid_apply_item_rel
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ApplyItemRel.class)
public class ApplyItemRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 外键关联报名记录主表ID
     */
    @ExcelProperty(value = "外键关联报名记录主表ID")
    private Long applyId;

    /**
     * 买家勾选要出价的具体的条目 ID
     */
    @ExcelProperty(value = "买家勾选要出价的具体的条目 ID")
    private Long itemId;

    /**
     * 该商品冻结保证金金额
     */
    @ExcelProperty(value = "该商品冻结保证金金额")
    private BigDecimal depositAmount;

}