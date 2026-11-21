package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.CouponGiftGrantRule;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 满赠券发放规则业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CouponGiftGrantRule.class, reverseConvertGenerate = false)
public class CouponGiftGrantRuleBo extends BaseEntity {

    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "促销名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /** 物权/归属编码 */
    private String memberCode;

    /** 物权/归属名称 */
    private String memberName;

    @NotNull(message = "订单实付金额门槛不能为空", groups = { AddGroup.class, EditGroup.class })
    @DecimalMin(value = "0.01", message = "实付门槛必须大于0", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal minOrderAmount;

    @NotNull(message = "优惠券模板ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long couponTemplateId;

    @NotNull(message = "赠送张数不能为空", groups = { AddGroup.class, EditGroup.class })
    @Min(value = 1, message = "赠送张数至少为1张", groups = { AddGroup.class, EditGroup.class })
    private Long giveCouponCount;

    /**
     * 用户人群范围: 0全部, 1指定用户ID, 2指定会员等级
     */
    @NotNull(message = "用户人群类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userScopeType;

    /**
     * 用户参与方式: 1白名单, 2黑名单
     */
    private Long userParticipateType;

    @NotNull(message = "活动开始时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startTime;

    @NotNull(message = "活动结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endTime;

    private String imgUrl;

    private Long status;

    private Long auditBy;

    private Date auditTime;

    private String auditRemark;

    private List<Long> userTargetIds;

}
