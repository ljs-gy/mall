package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.DistProductCommission;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品分佣配置业务对象 dist_product_commission
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistProductCommission.class, reverseConvertGenerate = false)
public class DistProductCommissionBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商品SPU ID
     */
    @NotNull(message = "商品SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /**
     * 分销员等级ID
     */
    @NotNull(message = "分销员等级ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long levelId;

    /**
     * 一级分佣比例
     */
    @NotNull(message = "一级分佣比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long firstRate;

    /**
     * 二级分佣比例
     */
    @NotNull(message = "二级分佣比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long secondRate;

    /**
     * 三级分佣比例
     */
    @NotNull(message = "三级分佣比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long thirdRate;


}
