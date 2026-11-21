package org.dromara.dis.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 渠道价格规则 dis_price_rule
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dis_price_rule")
public class PriceRule extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String channelCode;

    private String ruleName;

    /** 1按百分比，2按浮动金额，3按固定值 */
    private Long dpriceSyncType;

    private BigDecimal salesPriceRuleVal;

    private BigDecimal marketPriceRuleVal;

    private BigDecimal defaultSalesPrice;

    private BigDecimal defaultMarketPrice;

    /** 价格自动更新开关（0不同步，1同步） */
    private Long dpriceSync;

    /** 1两位小数，2一位，3整数 */
    private Long dpriceVer;

    private Long dpriceWeight;

    private Date dpriceSydate;

    private Date dpriceEydate;

    @TableLogic
    private String delFlag;
}
