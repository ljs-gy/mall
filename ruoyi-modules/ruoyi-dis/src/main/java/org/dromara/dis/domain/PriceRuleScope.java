package org.dromara.dis.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 价格规则适用范围对象 dis_price_rule_scope
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dis_price_rule_scope")
public class PriceRuleScope extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联价格规则ID
     */
    private Long priceRuleId;

    /**
     * 适用类型：1按商品，2按分类，3按品牌，4按门店
     */
    private Long scopeType;

    /**
     * 对应关联值
     */
    private String scopeValue;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
