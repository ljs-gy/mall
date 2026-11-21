package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品规格对象 goods_spec
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spec")
public class Spec extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规格主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联规格组ID
     */
    private Long specGroupId;

    /**
     * 规格名称（如：颜色、尺码）
     */
    private String specName;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
