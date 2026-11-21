package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 前后台分类映射关系对象 goods_category_front_back_rel
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_category_front_back_rel")
public class CategoryFrontBackRel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 前台类目ID
     */
    private Long frontendCategoryId;

    /**
     * 后台类目ID
     */
    private Long backendCategoryId;

}
