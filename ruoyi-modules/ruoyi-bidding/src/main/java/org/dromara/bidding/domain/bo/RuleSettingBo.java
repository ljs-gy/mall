package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.RuleSetting;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 竞价规则业务对象 bid_rule_setting
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = RuleSetting.class, reverseConvertGenerate = false)
public class RuleSettingBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "规则名称不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 规则名称
     */
    private String ruleName;

    @NotNull(message = "1-常规竞价，2-资质竞价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 1-常规竞价，2-资质竞价
     */
    private Integer bidType;

    @NotNull(message = "最少报名人数红线不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 最少报名人数红线
     */
    private Integer minApplyUsers;

    @NotNull(message = "可中标人数限制不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 可中标人数限制
     */
    private Integer maxWinnerUsers;

    @NotNull(message = "是否开启动态延时：0-关闭，1-开启不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 是否开启动态延时：0-关闭，1-开启
     */
    private Integer isDelayEnabled;

    /**
     * 单次延时触发周期（分钟）
     */
    private Integer delayTriggerMinutes;

    /**
     * 最大总延时时长
     */
    private Integer maxDelayMinutes;

    @NotNull(message = "平台佣金收取类型：0-不需要，1-按最终成交总额比例，2-固定额度/笔不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 平台佣金收取类型：0-不需要，1-按最终成交总额比例，2-固定额度/笔
     */
    private Integer commissionType;

    /**
     * 佣金数值
     */
    private BigDecimal commissionValue;

    @NotNull(message = "竞价模型：1-自由竞价, 2-公开竞价, 3-拆分竞价, 4-反向公开竞价, 5-反向自由竞价不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价模型：1-自由竞价, 2-公开竞价, 3-拆分竞价, 4-反向公开竞价, 5-反向自由竞价
     */
    private Integer bidModel;

    /**
     * 资质竞价要求的文本或范本描述
     */
    private String requiredQualification;

}