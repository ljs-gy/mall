package org.dromara.user.domain.bo;

import org.dromara.user.domain.BuyerAddress;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 买家收货地址业务对象 um_buyer_address
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BuyerAddress.class, reverseConvertGenerate = false)
public class BuyerAddressBo extends BaseEntity {

    /**
     * 地址主键ID
     */
    @NotNull(message = "地址主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 买家ID（um_userinfo.id）
     */
    @NotNull(message = "买家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeName;

    /**
     * 收货人手机号码
     */
    @NotBlank(message = "收货人手机号码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeMobile;

    /**
     * 省份编码
     */
    @NotBlank(message = "省份编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provinceCode;

    /**
     * 省份名称
     */
    @NotBlank(message = "省份名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provinceName;

    /**
     * 城市编码
     */
    @NotBlank(message = "城市编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cityCode;

    /**
     * 城市名称
     */
    @NotBlank(message = "城市名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cityName;

    /**
     * 区/县编码
     */
    @NotBlank(message = "区/县编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String districtCode;

    /**
     * 区/县名称
     */
    @NotBlank(message = "区/县名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String districtName;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String detailAddress;

    /**
     * 是否默认地址：0否，1是
     */
    @NotNull(message = "是否默认地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isDefault;

}
