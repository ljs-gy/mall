package org.dromara.pm.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.util.Date;
import java.util.List;

/**
 * 秒杀 / 特价 共用新增入参
 */
@Data
public class ExclusivePromoActivityBo {

    @NotBlank(message = "促销名称不能为空", groups = { AddGroup.class })
    @Size(max = 100, message = "促销名称不能超过100个字符", groups = { AddGroup.class })
    private String title;

    @NotBlank(message = "渠道编码不能为空", groups = { AddGroup.class })
    private String channelCode;

    private String imgUrl;

    @NotNull(message = "开始时间不能为空", groups = { AddGroup.class })
    private Date startTime;

    @NotNull(message = "结束时间不能为空", groups = { AddGroup.class })
    private Date endTime;

    /**
     * 订单未支付超时自动取消分钟数（秒杀必填；特价可省略走库默认）
     */
    @Min(value = 1, message = "取消时间至少为1分钟", groups = { AddGroup.class })
    private Long autoCancelMinutes;

    /**
     * 用户范围: 0全部用户, 1指定用户ID, 2指定会员等级（特价必填；秒杀可省略默认全部）
     */
    private Long userScopeType;

    /**
     * userScopeType 为 1 或 2 时必填
     */
    private List<Long> userTargetIds;

    @NotEmpty(message = "至少选择一个活动商品", groups = { AddGroup.class })
    @Valid
    private List<ExclusiveSkuItem> skuList;

}
