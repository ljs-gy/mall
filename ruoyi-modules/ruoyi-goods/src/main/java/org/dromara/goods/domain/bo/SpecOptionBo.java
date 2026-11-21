package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.SpecOption;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品规格选项业务对象 goods_spec_option
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SpecOption.class, reverseConvertGenerate = false)
public class SpecOptionBo extends BaseEntity {

    /**
     * 选项主键ID
     */
    @NotNull(message = "选项主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联规格ID
     */
    @NotNull(message = "关联规格ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long specId;

    /**
     * 规格选项名称
     */
    @NotBlank(message = "规格选项名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String specOptionName;

    /**
     * 备注
     */
    private String remark;


}
