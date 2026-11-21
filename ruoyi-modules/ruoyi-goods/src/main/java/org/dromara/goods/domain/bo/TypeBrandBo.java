package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.TypeBrand;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品类型与品牌关系业务对象 goods_type_brand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TypeBrand.class, reverseConvertGenerate = false)
public class TypeBrandBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商品类型ID
     */
    @NotNull(message = "商品类型ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long typeId;

    /**
     * 品牌ID
     */
    @NotNull(message = "品牌ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long brandId;


}
