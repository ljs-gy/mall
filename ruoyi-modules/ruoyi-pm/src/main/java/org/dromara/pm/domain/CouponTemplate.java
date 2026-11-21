package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;

import java.io.Serial;

/**
 * 优惠券对象 pm_coupon_template
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_coupon_template")
public class CouponTemplate extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 优惠券模板ID主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 优惠券名称
     */
    private String title;

    /**
     * 渠道编码（缓存命名空间：coupon:{tenantId}:{channelCode}:...）
     */
    private String channelCode;

    /**
     * 优惠券类型: REDUCE(满减券), DISCOUNT(折扣券)
     */
    private String couponType;

    /**
     * 发放方式: NORMAL(普通券), GIFT(满赠券), DIRECT(定向券)
     */
    private String grantType;

    /**
     * 活动图片URL
     */
    private String imgUrl;

    /**
     * 使用门槛: 最低消费金额
     */
    private BigDecimal minSpendAmount;

    /**
     * 优惠强度: 满减券表示减免面额(元), 折扣券表示折扣率(如0.85表示85折)
     */
    private BigDecimal benefitValue;

    /**
     * 优惠券总张数
     */
    private Long totalCount;

    /**
     * 每个会员ID可领取张数（-1表示不限制）
     */
    private Long userLimitCount;

    /**
     * 领用开始时间
     */
    private Date claimStartTime;

    /**
     * 领用结束时间
     */
    private Date claimEndTime;

    /**
     * 使用开始时间（固定时段；与 validMinutes 二选一）
     */
    private Date useStartTime;

    /**
     * 使用结束时间（固定时段；与 validMinutes 二选一）
     */
    private Date useEndTime;

    /**
     * 相对有效分钟数（领取后有效；与固定时段二选一）
     */
    private Long validMinutes;

    /**
     * 定向券每个用户发放张数
     */
    private Long directPerUserCount;

    /**
     * 定向券发送时间类型: 1立即发送, 2定时发送
     */
    private Long sendType;

    /**
     * 定向券定时发送时间
     */
    private Date sendTime;

    /**
     * 选择供应商: 0全部供应商, 1指定供应商
     */
    private Long scopeSupplierType;

    /**
     * 选择商品范围: 0全部商品, 1指定SKU, 2指定品类, 3指定品牌
     */
    private Long scopeItemType;

    /**
     * 商品参与方式: 1白名单, 2黑名单
     */
    private Long itemParticipateType;

    /**
     * 用户人群范围: 0全部用户, 1指定用户ID, 2指定会员等级
     */
    private Long userScopeType;

    /**
     * 用户参与方式: 1参与活动(白名单), 2不参与活动(黑名单)
     */
    private Long userParticipateType;

    /**
     * 平台分摊比例
     */
    private BigDecimal platformShareRate;

    /**
     * 商家分摊比例
     */
    private BigDecimal merchantShareRate;

    /**
     * 退款是否返回优惠券: 0否, 1是
     */
    private Long isRefundCoupon;

    /**
     * 优惠券使用说明
     */
    private String useInstructions;

    /**
     * 状态: 0待审核, 1未开始, 2进行中, 3已结束, 4已终止, 5审核失败
     */
    private Long status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 审核人
     */
    private Long auditBy;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核原因
     */
    private String auditRemark;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;


}
