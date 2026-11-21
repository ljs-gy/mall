package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Spec;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品规格业务对象 goods_spec
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Spec.class, reverseConvertGenerate = false)
public class SpecBo extends BaseEntity {

    /**
     * 规格主键ID
     */
    @NotNull(message = "规格主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联规格组ID
     */
    @NotNull(message = "关联规格组ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long specGroupId;

    /**
     * 规格名称（如：颜色、尺码）
     */
    @NotBlank(message = "规格名称（如：颜色、尺码）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String specName;


}
