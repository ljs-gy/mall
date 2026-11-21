package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品品牌对象 goods_brand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_brand")
public class Brand extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 品牌主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 品牌LOGO
     */
    private String logoUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
