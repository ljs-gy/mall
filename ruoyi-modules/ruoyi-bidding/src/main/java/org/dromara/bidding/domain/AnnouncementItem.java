package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 竞价标的对象 bid_announcement_item
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_announcement_item")
public class AnnouncementItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联场次活动主表ID
     */
    private Long sessionId;

    /**
     * 标的条目全网唯一业务编码
     */
    private String itemCode;

    /**
     * 标的属性：1-打包包裹（多商品组合）, 2-标准单品（单SKU）
     */
    private Integer itemType;

    /**
     * 关联物权资源库源头ID
     */
    private Long resourceId;

    /**
     * 包裹描述或商品单品展示名称
     */
    private String itemName;

    /**
     * 起拍基价
     */
    private BigDecimal pricesetNPrice;

    /**
     * 标的可供总数量/总重量
     */
    private BigDecimal goodsQuantity;

    /**
     * 起拍初始总金额
     */
    private BigDecimal pricesetPrefPrice;

    /**
     * 加价幅度
     */
    private BigDecimal auctionRuleAddPrice;

    /**
     * 单次最高出价限制，0为不限制
     */
    private BigDecimal maxBidLimit;

    /**
     * 属商品保证金金额
     */
    private BigDecimal itemDepositAmount;

    /**
     * 当前最高溢价
     */
    private BigDecimal auctionPriceNow;

    /**
     * 最高成交单价
     */
    private BigDecimal auctionGinfoPrice;

    /**
     * 当前最新成交总金额
     */
    private BigDecimal auctionGinfoMoney;

    /**
     * 第一名买家用户ID
     */
    private Long currentMaxUserId;

    /**
     * 累计有效被出价抢占次数
     */
    private Integer totalBidCount;

    /**
     * 条目运行状态：0-未开拍, 1-竞价中, 2-已落槌成交, 3-到期流拍, 4-中标后超时未补尾款触发违约
     */
    private Integer itemStatus;

    /**
     * 高并发防超卖与并发覆盖乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}