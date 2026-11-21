package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 买家收货地址对象 um_buyer_address
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_buyer_address")
public class BuyerAddress extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地址主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 买家ID（um_userinfo.id）
     */
    private Long userinfoId;

    /**
     * 收货人姓名
     */
    private String consigneeName;

    /**
     * 收货人手机号码
     */
    private String consigneeMobile;

    /**
     * 省份编码
     */
    private String provinceCode;

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区/县编码
     */
    private String districtCode;

    /**
     * 区/县名称
     */
    private String districtName;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 是否默认地址：0否，1是
     */
    private Long isDefault;

    /**
     * 删除标志：0存在，1删除
     */
    @TableLogic
    private String delFlag;

}
