package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 出价流水对象 bid_bid_log
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_bid_log")
public class BidLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 出价流水号
     */
    private String bidLogCode;

    /**
     * 竞价场次ID
     */
    private Long sessionId;

    /**
     * 标的明细ID
     */
    private Long itemId;

    /**
     * 买家用户ID
     */
    private Long userId;

    /**
     * 买家报出的单位重量溢价金额
     */
    private BigDecimal bidPrice;

    /**
     * 本次出价折算单价
     */
    private BigDecimal bidPriceUnit;

    /**
     * 折算出的成交总金额(总重量*折算单价)
     */
    private BigDecimal bidTotalMoney;

    /**
     * 应标意向数量/重量
     */
    private BigDecimal goodsQuantity;

    /**
     * 精准出价时刻
     */
    private Date bidTime;

    /**
     * 出价状态：1-出价成功暂列领先, 2-已被超越, 3-最终中标, 4-无效出价
     */
    private Integer bidStatus;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}