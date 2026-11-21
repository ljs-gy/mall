package org.dromara.dis.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 渠道用户范围设置 dis_dgoods_buy
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dis_dgoods_buy")
public class DgoodsBuy extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String channelCode;

    /** ORG / USER / PROVINCE / CITY / DISTRICT */
    private String dgoodsBuyType;

    /** = / != / IN / NOT_IN */
    private String dgoodsBuyTerm;

    private String dgoodsBuyValue;

    private String ruleValueName;

    @TableLogic
    private String delFlag;
}
