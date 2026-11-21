package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.CategoryFrontend;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 前台商品分类业务对象 goods_category_frontend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CategoryFrontend.class, reverseConvertGenerate = false)
public class CategoryFrontendBo extends BaseEntity {

    /**
     * 前台分类主键ID
     */
    @NotNull(message = "前台分类主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 前台分类名称
     */
    @NotBlank(message = "前台分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String frontCategoryName;

    /**
     * 父分类ID，0表示一级分类
     */
    @NotNull(message = "父分类ID，0表示一级分类不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long parentId;

    /**
     * 前台分类图片（仅三级有效）
     */
    private String logoUrl;

    /**
     * 是否隐藏（0否，1是）
     */
    @NotNull(message = "是否隐藏（0否，1是）不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isHidden;

    /**
     * 关联的后台三级分类ID列表（仅三级前台分类有效）
     */
    private List<Long> backendCategoryIds;

}
