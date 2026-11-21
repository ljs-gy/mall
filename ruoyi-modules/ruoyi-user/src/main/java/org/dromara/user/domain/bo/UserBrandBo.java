package org.dromara.user.domain.bo;

import org.dromara.user.domain.UserBrand;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 用户经营品牌资质业务对象 um_user_brand
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserBrand.class, reverseConvertGenerate = false)
public class UserBrandBo extends BaseEntity {

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
     * 业务类型：merchant(商家经营品牌), supplier(供应商供货品牌)
     */
    @NotBlank(message = "业务类型：merchant(商家经营品牌), supplier(供应商供货品牌)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bizType;

    /**
     * 品牌ID
     */
    @NotNull(message = "品牌ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long brandId;

    /**
     * 品牌授权书图片
     */
    private String authLetterUrl;

    /**
     * 审核状态：0待审核，1已通过，2已驳回
     */
    @NotNull(message = "审核状态：0待审核，1已通过，2已驳回不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditStatus;


}
