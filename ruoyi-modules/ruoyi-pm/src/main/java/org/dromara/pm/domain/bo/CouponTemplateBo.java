package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.CouponTemplate;
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
 * 优惠券业务对象 pm_coupon_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CouponTemplate.class, reverseConvertGenerate = false)
public class CouponTemplateBo extends BaseEntity {

    @NotNull(message = "优惠券模板ID主键不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "优惠券名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    @NotBlank(message = "优惠券类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String couponType;

    @NotBlank(message = "发放方式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grantType;

    private String imgUrl;

    @NotNull(message = "使用门槛金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal minSpendAmount;

    @NotNull(message = "优惠强度不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal benefitValue;

    private Long totalCount;

    /**
     * 每个会员ID可领取张数（-1表示不限制）
     */
    private Long userLimitCount;

    private Date claimStartTime;

    private Date claimEndTime;

    /** 固定时段开始（与 validMinutes 二选一） */
    private Date useStartTime;

    /** 固定时段结束（与 validMinutes 二选一） */
    private Date useEndTime;

    /** 领取后有效分钟数（与固定时段二选一） */
    private Long validMinutes;

    private Long directPerUserCount;

    private Long sendType;

    private Date sendTime;

    private Long scopeSupplierType;

    /**
     * 选择商品范围: 0全部, 1指定SKU, 2指定品类, 3指定品牌
     */
    @NotNull(message = "商品范围类型不能为空", groups = { AddGroup.class })
    private Long scopeItemType;

    /**
     * 商品参与方式: 1白名单, 2黑名单
     */
    private Long itemParticipateType;

    /**
     * 用户人群范围: 0全部, 1指定用户ID, 2指定会员等级
     */
    @NotNull(message = "用户人群范围不能为空", groups = { AddGroup.class })
    private Long userScopeType;

    private Long userParticipateType;

    /**
     * 平台分摊比例（跨店券必填；普通券可不传，库默认 0）
     */
    private BigDecimal platformShareRate;

    /**
     * 商家分摊比例（跨店券必填；普通券可不传，库默认 0）
     */
    private BigDecimal merchantShareRate;

    @NotNull(message = "退款是否返回优惠券不能为空", groups = { AddGroup.class })
    private Long isRefundCoupon;

    private String useInstructions;

    private Long status;

    private Integer version;

    private Long auditBy;

    private Date auditTime;

    private String auditRemark;

    /** 指定供应商 userinfoCode 列表（与商品 member_ccode 对齐） */
    private List<String> supplierCodes;

    private List<Long> itemIds;

    private List<Long> userTargetIds;

}
