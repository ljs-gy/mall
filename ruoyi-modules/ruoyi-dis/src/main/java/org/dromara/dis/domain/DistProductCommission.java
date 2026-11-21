package org.dromara.dis.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品分佣配置对象 dist_product_commission
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dist_product_commission")
public class DistProductCommission extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 分销员等级ID
     */
    private Long levelId;

    /**
     * 一级分佣比例
     */
    private Long firstRate;

    /**
     * 二级分佣比例
     */
    private Long secondRate;

    /**
     * 三级分佣比例
     */
    private Long thirdRate;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
