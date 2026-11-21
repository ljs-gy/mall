package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品规格选项对象 goods_spec_option
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spec_option")
public class SpecOption extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 选项主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联规格ID
     */
    private Long specId;

    /**
     * 规格选项名称
     */
    private String specOptionName;

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
