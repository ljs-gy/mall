package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import java.io.Serial;

/**
 * 用户优惠券领用记录对象 pm_coupon_user_rel
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_coupon_user_rel")
public class CouponUserRel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户优惠券ID主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券模板ID
     */
    private Long couponTemplateId;

    /**
     * 渠道编码快照
     */
    private String channelCode;

    /**
     * 券名称快照
     */
    private String title;

    /**
     * 优惠券类型快照: REDUCE/DISCOUNT
     */
    private String couponType;

    /**
     * 活动图片快照
     */
    private String imgUrl;

    /**
     * 用户券唯一流水码
     */
    private String userCouponCode;

    /**
     * 门槛快照: 最低消费金额
     */
    private java.math.BigDecimal minSpendAmount;

    /**
     * 面额/折扣快照
     */
    private java.math.BigDecimal benefitValue;

    /**
     * 有效期开始时间
     */
    private Date startTime;

    /**
     * 有效期结束时间
     */
    private Date endTime;

    /**
     * 使用状态: 0未使用, 1已使用, 2已过期, 3已作废
     */
    private Long status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

}
