package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.DistLevel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 分销员等级业务对象 dist_level
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistLevel.class, reverseConvertGenerate = false)
public class DistLevelBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 等级名称
     */
    @NotBlank(message = "等级名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String levelName;

    /**
     * 自购金额满多少元
     */
    private java.math.BigDecimal upgradeSelfAmount;

    /**
     * 推广客户满多少人
     */
    private Long upgradeInviteCount;

    /**
     * 累计佣金满多少元
     */
    private java.math.BigDecimal upgradeTotalCommission;

    /**
     * 等级权重
     */
    @NotNull(message = "等级权重不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long levelWeight;

    /**
     * 一级分佣比例
     */
    private java.math.BigDecimal firstRate;

    /**
     * 二级分佣比例
     */
    private java.math.BigDecimal secondRate;

    /**
     * 三级分佣比例
     */
    private java.math.BigDecimal thirdRate;


}
