package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.CouponUserRel;
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
 * 用户优惠券领用记录业务对象 pm_coupon_user_rel
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CouponUserRel.class, reverseConvertGenerate = false)
public class CouponUserRelBo extends BaseEntity {

    /**
     * 用户优惠券ID主键
     */
    @NotNull(message = "用户优惠券ID主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 优惠券模板ID
     */
    @NotNull(message = "优惠券模板ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long couponTemplateId;

    /** 渠道编码快照 */
    private String channelCode;

    /** 券名称快照 */
    private String title;

    /** 优惠券类型快照 */
    private String couponType;

    /** 活动图片快照 */
    private String imgUrl;

    /**
     * 用户券唯一流水码
     */
    @NotBlank(message = "用户券唯一流水码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userCouponCode;

    /**
     * 门槛快照: 最低消费金额
     */
    @NotNull(message = "门槛快照: 最低消费金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal minSpendAmount;

    /**
     * 面额/折扣快照
     */
    @NotNull(message = "面额/折扣快照不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal benefitValue;

    /**
     * 有效期开始时间
     */
    @NotNull(message = "有效期开始时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startTime;

    /**
     * 有效期结束时间
     */
    @NotNull(message = "有效期结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endTime;

    /**
     * 使用状态: 0未使用, 1已使用, 2已过期, 3已作废
     */
    @NotNull(message = "使用状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

}
