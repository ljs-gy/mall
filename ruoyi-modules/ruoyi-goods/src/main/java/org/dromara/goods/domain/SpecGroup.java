package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品规格组对象 goods_spec_group
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spec_group")
public class SpecGroup extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格组主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联商品类型ID
     */
    private Long typeId;

    /**
     * 规格组名称
     */
    private String specGroupName;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
