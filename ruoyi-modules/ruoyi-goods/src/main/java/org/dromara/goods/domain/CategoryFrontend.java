package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 前台商品分类对象 goods_category_frontend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_category_frontend")
public class CategoryFrontend extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 前台分类主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 前台分类名称
     */
    private String frontCategoryName;

    /**
     * 父分类ID，0表示一级分类
     */
    private Long parentId;

    /**
     * 前台分类图片（仅三级有效）
     */
    private String logoUrl;

    /**
     * 是否隐藏（0否，1是）
     */
    private Long isHidden;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
