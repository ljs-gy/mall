package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Type;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 商品类型业务对象 goods_type
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Type.class, reverseConvertGenerate = false)
public class TypeBo extends BaseEntity {

    /**
     * 类型主键ID
     */
    @NotNull(message = "类型主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 类型名称
     */
    @NotBlank(message = "类型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String typeName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联品牌ID列表
     */
    private List<Long> brandIds;

}
