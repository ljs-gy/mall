package org.dromara.user.domain.bo;

import org.dromara.user.domain.UserCategory;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 用户经营类目资质业务对象 um_user_category
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserCategory.class, reverseConvertGenerate = false)
public class UserCategoryBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户信息ID
     */
    @NotNull(message = "用户信息ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 业务类型：merchant(商家售卖类目), supplier(供应商供货类目)
     */
    @NotBlank(message = "业务类型：merchant(商家售卖类目), supplier(供应商供货类目)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bizType;

    /**
     * 商品平台类目ID
     */
    @NotNull(message = "商品平台类目ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long categoryId;

    /**
     * 审核状态：0待审核，1已通过，2已驳回
     */
    @NotNull(message = "审核状态：0待审核，1已通过，2已驳回不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditStatus;


}
