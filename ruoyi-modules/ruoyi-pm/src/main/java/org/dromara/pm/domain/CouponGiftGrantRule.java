package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 满赠券发放规则对象 pm_coupon_gift_grant_rule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_coupon_gift_grant_rule")
public class CouponGiftGrantRule extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /**
     * 满赠券促销名称
     */
    private String title;

    /**
     * 渠道编码（缓存命名空间：coupon:{tenantId}:{channelCode}:...）
     */
    private String channelCode;

    /**
     * 物权/归属编码
     */
    private String memberCode;

    /**
     * 物权/归属名称
     */
    private String memberName;

    /**
     * 发放门槛: 订单实付金额满多少元
     */
    private BigDecimal minOrderAmount;

    /**
     * 关联赠送的优惠券模板ID
     */
    private Long couponTemplateId;

    /**
     * 赠送优惠券张数
     */
    private Long giveCouponCount;

    /**
     * 用户人群范围: 0全部用户, 1指定用户ID, 2指定会员等级
     */
    private Long userScopeType;

    /**
     * 用户参与方式: 1白名单, 2黑名单
     */
    private Long userParticipateType;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 活动图片
     */
    private String imgUrl;

    /**
     * 状态: 0待审核, 1未开始, 2进行中, 3已结束, 4已终止, 5已驳回
     */
    private Long status;

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

    @TableLogic
    private String delFlag;

}
