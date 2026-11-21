package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.CategoryBackend;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 后台商品分类业务对象 goods_category_backend
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CategoryBackend.class, reverseConvertGenerate = false)
public class CategoryBackendBo extends BaseEntity {

    /**
     * 分类主键ID
     */
    @NotNull(message = "分类主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String categoryName;

    /**
     * 父分类ID，0表示一级分类
     */
    @NotNull(message = "父分类ID，0表示一级分类不能为空", groups = { AddGroup.class, EditGroup.class })
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


}
