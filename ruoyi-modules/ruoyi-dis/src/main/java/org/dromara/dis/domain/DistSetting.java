package org.dromara.dis.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 分销基础配置对象 dist_setting
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dist_setting")
public class DistSetting extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 基础一级分佣比例
     */
    private BigDecimal baseFirstRate;

    /**
     * 基础二级分佣比例
     */
    private BigDecimal baseSecondRate;

    /**
     * 基础三级分佣比例
     */
    private BigDecimal baseThirdRate;

    /**
     * 返佣资质：0-所有人均可获得返佣，1-仅分销员用户获得返佣
     */
    private Long qualifyType;

    /**
     * 分销员自己购买商品是否有佣金：0-无佣金，1-有佣金
     */
    private Long isBuyerHasCommission;

    /**
     * 加入分销页面背景图
     */
    private String joinBgUrl;

    /**
     * 邀请注册页面背景图
     */
    private String inviteBgUrl;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
