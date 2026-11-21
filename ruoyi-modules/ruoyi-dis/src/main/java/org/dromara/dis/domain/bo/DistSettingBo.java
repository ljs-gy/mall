package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.DistSetting;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 分销基础配置业务对象 dist_setting
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistSetting.class, reverseConvertGenerate = false)
public class DistSettingBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 基础一级分佣比例
     */
    @NotNull(message = "基础一级分佣比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private java.math.BigDecimal baseFirstRate;

    /**
     * 基础二级分佣比例
     */
    @NotNull(message = "基础二级分佣比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private java.math.BigDecimal baseSecondRate;

    /**
     * 基础三级分佣比例
     */
    @NotNull(message = "基础三级分佣比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private java.math.BigDecimal baseThirdRate;

    /**
     * 返佣资质：0-所有人均可获得返佣，1-仅分销员用户获得返佣
     */
    @NotNull(message = "返佣资质：0-所有人均可获得返佣，1-仅分销员用户获得返佣不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long qualifyType;

    /**
     * 分销员自己购买商品是否有佣金：0-无佣金，1-有佣金
     */
    @NotNull(message = "分销员自己购买商品是否有佣金：0-无佣金，1-有佣金不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isBuyerHasCommission;

    /**
     * 加入分销页面背景图
     */
    private String joinBgUrl;

    /**
     * 邀请注册页面背景图
     */
    private String inviteBgUrl;


}
