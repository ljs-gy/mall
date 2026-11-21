package org.dromara.user.domain.vo;

import org.dromara.user.domain.BuyerAddress;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 买家收货地址视图对象 um_buyer_address
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BuyerAddress.class)
public class BuyerAddressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 地址主键ID
     */
    @ExcelProperty(value = "地址主键ID")
    private Long id;

    /**
     * 买家ID（um_userinfo.id）
     */
    @ExcelProperty(value = "买家ID")
    private Long userinfoId;

    /**
     * 收货人姓名
     */
    @ExcelProperty(value = "收货人姓名")
    private String consigneeName;

    /**
     * 收货人手机号码
     */
    @ExcelProperty(value = "收货人手机号码")
    private String consigneeMobile;

    /**
     * 省份编码
     */
    @ExcelProperty(value = "省份编码")
    private String provinceCode;

    /**
     * 省份名称
     */
    @ExcelProperty(value = "省份名称")
    private String provinceName;

    /**
     * 城市编码
     */
    @ExcelProperty(value = "城市编码")
    private String cityCode;

    /**
     * 城市名称
     */
    @ExcelProperty(value = "城市名称")
    private String cityName;

    /**
     * 区/县编码
     */
    @ExcelProperty(value = "区/县编码")
    private String districtCode;

    /**
     * 区/县名称
     */
    @ExcelProperty(value = "区/县名称")
    private String districtName;

    /**
     * 详细地址
     */
    @ExcelProperty(value = "详细地址")
    private String detailAddress;

    /**
     * 是否默认地址：0否，1是
     */
    @ExcelProperty(value = "是否默认地址")
    private Long isDefault;

}
