package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 后台商品分类对象 goods_category_backend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_category_backend")
public class CategoryBackend extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父分类ID，0表示一级分类
     */
    private Long parentId;

    /**
     * 关联商品类型ID（仅三级分类有效）
     */
    private Long typeId;

    /**
     * 分类logo（仅三级分类有效）
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
