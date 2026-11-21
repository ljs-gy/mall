package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.BidLog;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.math.BigDecimal;

/**
 * 出价流水业务对象 bid_bid_log
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BidLog.class, reverseConvertGenerate = false)
public class BidLogBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "出价流水号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 出价流水号
     */
    private String bidLogCode;

    @NotNull(message = "竞价场次ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价场次ID
     */
    private Long sessionId;

    @NotNull(message = "标的明细ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 标的明细ID
     */
    private Long itemId;

    @NotNull(message = "买家用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 买家用户ID
     */
    private Long userId;

    @NotNull(message = "买家报出的单位重量溢价金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 买家报出的单位重量溢价金额
     */
    private BigDecimal bidPrice;

    @NotNull(message = "本次出价折算单价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 本次出价折算单价
     */
    private BigDecimal bidPriceUnit;

    /**
     * 折算出的成交总金额(总重量*折算单价)
     */
    private BigDecimal bidTotalMoney;

    @NotNull(message = "应标意向数量/重量不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 应标意向数量/重量
     */
    private BigDecimal goodsQuantity;

    @NotNull(message = "精准出价时刻不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 精准出价时刻
     */
    private Date bidTime;

    @NotNull(message = "出价状态：1-出价成功暂列领先, 2-已被超越, 3-最终中标, 4-无效出价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 出价状态：1-出价成功暂列领先, 2-已被超越, 3-最终中标, 4-无效出价
     */
    private Integer bidStatus;

}