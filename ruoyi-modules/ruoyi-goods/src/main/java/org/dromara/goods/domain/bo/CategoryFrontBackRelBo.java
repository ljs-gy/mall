package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.CategoryFrontBackRel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 前后台分类映射关系业务对象 goods_category_front_back_rel
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CategoryFrontBackRel.class, reverseConvertGenerate = false)
public class CategoryFrontBackRelBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 前台类目ID
     */
    @NotNull(message = "前台类目ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long frontendCategoryId;

    /**
     * 后台类目ID
     */
    @NotNull(message = "后台类目ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long backendCategoryId;

}
