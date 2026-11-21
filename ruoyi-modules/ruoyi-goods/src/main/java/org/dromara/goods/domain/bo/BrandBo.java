package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Brand;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品品牌业务对象 goods_brand
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Brand.class, reverseConvertGenerate = false)
public class BrandBo extends BaseEntity {

    /**
     * 品牌主键ID
     */
    @NotNull(message = "品牌主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 品牌名称
     */
    @NotBlank(message = "品牌名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String brandName;

    /**
     * 品牌LOGO
     */
    private String logoUrl;

    /**
     * 备注
     */
    private String remark;


}
