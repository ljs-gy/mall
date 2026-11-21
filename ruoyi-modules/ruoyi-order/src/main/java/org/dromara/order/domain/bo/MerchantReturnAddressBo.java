package org.dromara.order.domain.bo;

import org.dromara.order.domain.MerchantReturnAddress;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商家退货收货地址库业务对象 oc_merchant_return_address
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = MerchantReturnAddress.class, reverseConvertGenerate = false)
public class MerchantReturnAddressBo extends BaseEntity {

    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "物权编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    @NotBlank(message = "收件人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeName;

    @NotBlank(message = "收件人手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String consigneeMobile;

    private String provinceCode;

    @NotBlank(message = "省份名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provinceName;

    private String cityCode;

    @NotBlank(message = "城市名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cityName;

    private String districtCode;

    @NotBlank(message = "区县名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String districtName;

    @NotBlank(message = "详细地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String detailAddress;

    private Long isDefault;


}
