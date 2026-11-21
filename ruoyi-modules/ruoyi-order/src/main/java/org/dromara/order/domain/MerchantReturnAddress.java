package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商家退货收货地址库对象 oc_merchant_return_address
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_merchant_return_address")
public class MerchantReturnAddress extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 归属商家/物权编码 */
    private String memberCode;

    /** 收件人姓名 */
    private String consigneeName;

    /** 收件人手机号 */
    private String consigneeMobile;

    /** 省份编码 */
    private String provinceCode;

    /** 省份名称 */
    private String provinceName;

    /** 城市编码 */
    private String cityCode;

    /** 城市名称 */
    private String cityName;

    /** 区县编码 */
    private String districtCode;

    /** 区县名称 */
    private String districtName;

    /** 详细地址 */
    private String detailAddress;

    /** 是否默认地址: 0否, 1是 */
    private Long isDefault;

    @TableLogic
    private String delFlag;


}
