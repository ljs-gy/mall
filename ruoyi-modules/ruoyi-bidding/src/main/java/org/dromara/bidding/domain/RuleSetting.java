package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 竞价规则对象 bid_rule_setting
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_rule_setting")
public class RuleSetting extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 1-常规竞价，2-资质竞价
     */
    private Integer bidType;

    /**
     * 最少报名人数红线
     */
    private Integer minApplyUsers;

    /**
     * 可中标人数限制
     */
    private Integer maxWinnerUsers;

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

    /**
     * 平台佣金收取类型：0-不需要，1-按最终成交总额比例，2-固定额度/笔
     */
    private Integer commissionType;

    /**
     * 佣金数值
     */
    private BigDecimal commissionValue;

    /**
     * 竞价模型：1-自由竞价, 2-公开竞价, 3-拆分竞价, 4-反向公开竞价, 5-反向自由竞价
     */
    private Integer bidModel;

    /**
     * 资质竞价要求的文本或范本描述
     */
    private String requiredQualification;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}