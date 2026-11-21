package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞价标的业务对象 bid_announcement_item
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AnnouncementItem.class, reverseConvertGenerate = false)
public class AnnouncementItemBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotNull(message = "关联场次活动主表ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联场次活动主表ID
     */
    private Long sessionId;

    @NotBlank(message = "标的条目全网唯一业务编码不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 标的条目全网唯一业务编码
     */
    private String itemCode;

    @NotNull(message = "标的属性：1-打包包裹（多商品组合）, 2-标准单品（单SKU）不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 标的属性：1-打包包裹（多商品组合）, 2-标准单品（单SKU）
     */
    private Integer itemType;

    @NotNull(message = "关联物权资源库源头ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联物权资源库源头ID
     */
    private Long resourceId;

    @NotBlank(message = "包裹描述或商品单品展示名称不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 包裹描述或商品单品展示名称
     */
    private String itemName;

    @NotNull(message = "起拍基价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 起拍基价
     */
    private BigDecimal pricesetNPrice;

    @NotNull(message = "标的可供总数量/总重量不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 标的可供总数量/总重量
     */
    private BigDecimal goodsQuantity;

    @NotNull(message = "起拍初始总金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 起拍初始总金额
     */
    private BigDecimal pricesetPrefPrice;

    @NotNull(message = "加价幅度不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 加价幅度
     */
    private BigDecimal auctionRuleAddPrice;

    @NotNull(message = "单次最高出价限制，0为不限制不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 单次最高出价限制，0为不限制
     */
    private BigDecimal maxBidLimit;

    /**
     * 属商品保证金金额
     */
    private BigDecimal itemDepositAmount;

    @NotNull(message = "当前最高溢价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 当前最高溢价
     */
    private BigDecimal auctionPriceNow;

    @NotNull(message = "最高成交单价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 最高成交单价
     */
    private BigDecimal auctionGinfoPrice;

    @NotNull(message = "当前最新成交总金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 当前最新成交总金额
     */
    private BigDecimal auctionGinfoMoney;

    /**
     * 第一名买家用户ID
     */
    private Long currentMaxUserId;

    @NotNull(message = "累计有效被出价抢占次数不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 累计有效被出价抢占次数
     */
    private Integer totalBidCount;

    @NotNull(message = "条目运行状态：0-未开拍, 1-竞价中, 2-已落槌成交, 3-到期流拍, 4-中标后超时未补尾款触发违约不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 条目运行状态：0-未开拍, 1-竞价中, 2-已落槌成交, 3-到期流拍, 4-中标后超时未补尾款触发违约
     */
    private Integer itemStatus;

    /**
     * 初始原定截拍截止时间
     */
    private Date originalEndTime;

    /**
     * 当前包裹独立动态截拍截止时间
     */
    private Date itemEndTime;

    /**
     * 高并发防超卖与并发覆盖乐观锁版本号
     */
    private Integer version;

}