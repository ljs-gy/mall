package org.dromara.user.domain.bo;

import org.dromara.user.domain.Store;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商家店铺业务对象 um_store
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Store.class, reverseConvertGenerate = false)
public class StoreBo extends BaseEntity {

    /**
     * 店铺ID
     */
    @NotNull(message = "店铺ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 所属用户信息ID
     */
    @NotNull(message = "所属用户信息ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 店铺唯一编号
     */
    @NotBlank(message = "店铺唯一编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String storeNo;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String storeName;

    /**
     * 店铺Logo
     */
    private String storeLogoUrl;

    /**
     * 店铺简介
     */
    private String storeIntro;

    /**
     * 店铺状态：1正常，0关闭
     */
    @NotNull(message = "店铺状态：1正常，0关闭不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;


}
