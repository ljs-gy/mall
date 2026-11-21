package org.dromara.pm.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.pm.domain.PromoActivity;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 营销活动主视图对象 pm_promo_activity
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PromoActivity.class)
public class PromoActivityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID主键
     */
    @ExcelProperty(value = "活动ID主键")
    private Long id;

    /**
     * 促销活动名称
     */
    @ExcelProperty(value = "促销活动名称")
    private String title;

    /**
     * 渠道编码
     */
    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    /**
     * 物权/归属编码
     */
    @ExcelProperty(value = "物权编码")
    private String memberCode;

    /**
     * 物权/归属名称
     */
    @ExcelProperty(value = "物权名称")
    private String memberName;

    /**
     * 促销类型: FULL_REDUCE(满减), FULL_DISCOUNT(满折), FULL_GIFT(满赠), SECKILL(秒杀), SPECIAL_PRICE(特价), CROSS_FULL_REDUCE(跨店满减), CROSS_FULL_DISCOUNT(跨店满折)
     */
    @ExcelProperty(value = "促销类型: FULL_REDUCE(满减), FULL_DISCOUNT(满折), FULL_GIFT(满赠), SECKILL(秒杀), SPECIAL_PRICE(特价), CROSS_FULL_REDUCE(跨店满减), CROSS_FULL_DISCOUNT(跨店满折)")
    private String promoType;

    /**
     * 计算维度: 1按金额(满X元), 2按件数(满X件)
     */
    @ExcelProperty(value = "计算维度: 1按金额(满X元), 2按件数(满X件)")
    private Long calcType;

    /**
     * 活动宣传图片URL
     */
    @ExcelProperty(value = "活动宣传图片URL")
    private String imgUrl;

    /**
     * 活动开始时间
     */
    @ExcelProperty(value = "活动开始时间")
    private Date startTime;

    /**
     * 活动结束时间
     */
    @ExcelProperty(value = "活动结束时间")
    private Date endTime;

    /**
     * 是否上不封顶: 0否, 1是 (仅满减/跨店满减有效)
     */
    @ExcelProperty(value = "是否上不封顶: 0否, 1是 (仅满减/跨店满减有效)")
    private Long isCap;

    /**
     * 选择供应商: 0全部供应商, 1指定供应商
     */
    @ExcelProperty(value = "选择供应商: 0全部供应商, 1指定供应商")
    private Long scopeSupplierType;

    /**
     * 选择商品范围: 0全部, 1SKU, 2品类, 3品牌
     */
    @ExcelProperty(value = "选择商品范围")
    private Long scopeItemType;

    /**
     * 商品参与方式: 1白名单, 2黑名单
     */
    @ExcelProperty(value = "商品参与方式")
    private Long itemParticipateType;

    /**
     * 用户人群范围: 0全部, 1用户ID, 2会员等级
     */
    @ExcelProperty(value = "用户人群范围")
    private Long userScopeType;

    /**
     * 用户参与方式: 1参与活动(白名单), 2不参与活动(黑名单)
     */
    @ExcelProperty(value = "用户参与方式: 1参与活动(白名单), 2不参与活动(黑名单)")
    private Long userParticipateType;

    /**
     * 每个会员ID购买限制 (-1表示不限次数)
     */
    @ExcelProperty(value = "每个会员ID购买限制 (-1表示不限次数)")
    private Long userLimitQty;

    /**
     * 活动预算总额(跨店营销用)
     */
    @ExcelProperty(value = "活动预算总额(跨店营销用)")
    private java.math.BigDecimal budgetTotal;

    /**
     * 平台分摊比例 (如0.3000表示30%)
     */
    @ExcelProperty(value = "平台分摊比例 (如0.3000表示30%)")
    private java.math.BigDecimal platformShareRate;

    /**
     * 商家分摊比例 (如0.7000表示70%)
     */
    @ExcelProperty(value = "商家分摊比例 (如0.7000表示70%)")
    private java.math.BigDecimal merchantShareRate;

    /**
     * 退款是否返现: 0否, 1是
     */
    @ExcelProperty(value = "退款是否返现: 0否, 1是")
    private Long isRefundCash;

    /**
     * 状态: 0草稿, 1未开始, 2进行中, 3已结束, 4已下架
     */
    @ExcelProperty(value = "状态: 0待审核, 1未开始, 2进行中, 3已结束, 4已终止, 5已驳回")
    private Long status;

    @ExcelProperty(value = "审核人")
    private Long auditBy;

    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    @ExcelProperty(value = "审核原因")
    private String auditRemark;

    /**
     * 订单未支付超时自动取消时间
     */
    @ExcelProperty(value = "订单未支付超时自动取消时间")
    private Long autoCancelMinutes;


}
