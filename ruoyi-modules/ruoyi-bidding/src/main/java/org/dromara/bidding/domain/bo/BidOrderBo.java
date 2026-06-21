package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.BidOrder;
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
 * 竞价订单业务对象 bid_order
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BidOrder.class, reverseConvertGenerate = false)
public class BidOrderBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "竞价订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价订单号
     */
    private String orderNo;

    @NotNull(message = "所属竞价场次公告ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 所属竞价场次公告ID
     */
    private Long sessionId;

    @NotNull(message = "中标明细条目ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 中标明细条目ID
     */
    private Long itemId;

    @NotNull(message = "中标买家用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 中标买家用户ID
     */
    private Long userId;

    @NotNull(message = "中标成交单价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 中标成交单价
     */
    private BigDecimal strikePrice;

    @NotNull(message = "成交总重量/总数量不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 成交总重量/总数量
     */
    private BigDecimal quantity;

    @NotNull(message = "商品总价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 商品总价
     */
    private BigDecimal totalGoodsAmount;

    @NotNull(message = "保证金转货款抵扣金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 保证金转货款抵扣金额
     */
    private BigDecimal depositOffsetAmount;

    @NotNull(message = "平台佣金服务费不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 平台佣金服务费
     */
    private BigDecimal commissionAmount;

    @NotNull(message = "最终买家应补尾款不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 最终买家应补尾款
     */
    private BigDecimal finalPendingAmount;

    @NotNull(message = "履约状态：0-中标待补尾款及佣金, 1-尾款佣金双清结, 2-已出库, 3-已收货, 4-超时未补尾不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 履约状态：0-中标待补尾款及佣金, 1-尾款佣金双清结, 2-已出库, 3-已收货, 4-超时未补尾
     */
    private Integer orderStatus;

    /**
     * 清结时间
     */
    private Date payTime;

    @NotNull(message = "买家补尾款倒计时不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 买家补尾款倒计时
     */
    private Date expireTime;

}