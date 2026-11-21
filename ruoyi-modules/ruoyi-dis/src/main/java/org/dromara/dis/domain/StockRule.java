package org.dromara.dis.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 渠道库存规则对象 dis_stock_rule
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dis_stock_rule")
public class StockRule extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联渠道编码
     */
    private String channelCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 库存同步规则：1按百分比，2按固定值
     */
    private Long dgnumPro;

    /**
     * 库存数值/百分比例值
     */
    private BigDecimal dgnumPrice;

    /**
     * 库存自动更新开关（0否，1是）
     */
    private Long dgnumSync;

    /**
     * 安全库存最低阈值
     */
    private Long dgnumMin;

    /**
     * 库存计算取整规则：1取整，2四舍五入到一位，3四舍五入到两位，0不处理
     */
    private Long dgnumVer;

    /**
     * 权重，越小越优先
     */
    private Long dgnumWeight;

    /**
     * 生效时间
     */
    private Date dgnumStydate;

    /**
     * 失效时间
     */
    private Date dgnumEydate;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
