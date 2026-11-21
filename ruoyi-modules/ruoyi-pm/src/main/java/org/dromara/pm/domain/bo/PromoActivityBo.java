package org.dromara.pm.domain.bo;

import org.dromara.pm.domain.PromoActivity;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 营销活动主业务对象 pm_promo_activity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PromoActivity.class, reverseConvertGenerate = false)
public class PromoActivityBo extends BaseEntity {

    @NotNull(message = "活动ID主键不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "促销活动名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /** 物权/归属编码 */
    private String memberCode;

    /** 物权/归属名称 */
    private String memberName;

    @NotBlank(message = "促销类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String promoType;

    @NotNull(message = "计算维度不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long calcType;

    private String imgUrl;

    @NotNull(message = "活动开始时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startTime;

    @NotNull(message = "活动结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endTime;

    private Long isCap;

    private Long scopeSupplierType;


    private Long scopeItemType;

    private Long itemParticipateType;


    private Long userScopeType;

    private Long userParticipateType;

    private Long userLimitQty;

    private BigDecimal budgetTotal;

    private BigDecimal platformShareRate;

    private BigDecimal merchantShareRate;

    private Long isRefundCash;

    private Long status;

    private Long auditBy;

    private Date auditTime;

    private String auditRemark;

    private Long autoCancelMinutes;

    @NotEmpty(message = "至少配置一条阶梯规则", groups = { AddGroup.class })
    @Valid
    private List<PromoTierRuleItemBo> tierRules;

    /** 指定供应商 userinfoCode 列表（与商品 member_ccode 对齐） */
    private List<String> supplierCodes;

    private List<Long> itemIds;

    private List<Long> userTargetIds;

}
