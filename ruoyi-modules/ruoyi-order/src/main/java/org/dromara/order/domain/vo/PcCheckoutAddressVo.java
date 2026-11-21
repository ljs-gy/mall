package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 结算确认页 - 收货地址
 */
@Data
public class PcCheckoutAddressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 地址主键 ID（um_buyer_address.id） */
    private Long addressId;
    /** 收货人姓名 */
    private String consigneeName;
    /** 收货人手机号 */
    private String consigneeMobile;
    /** 省份名称 */
    private String provinceName;
    /** 城市名称 */
    private String cityName;
    /** 区/县名称 */
    private String districtName;
    /** 详细地址 */
    private String detailAddress;
    /** 是否默认地址：0否，1是 */
    private Integer isDefault;
}
