package org.dromara.order.domain.vo;

import org.dromara.order.domain.MerchantReturnAddress;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家退货收货地址库视图对象 oc_merchant_return_address
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantReturnAddress.class)
public class MerchantReturnAddressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "物权编码")
    private String memberCode;

    @ExcelProperty(value = "收件人姓名")
    private String consigneeName;

    @ExcelProperty(value = "收件人手机号")
    private String consigneeMobile;

    @ExcelProperty(value = "省份编码")
    private String provinceCode;

    @ExcelProperty(value = "省份名称")
    private String provinceName;

    @ExcelProperty(value = "城市编码")
    private String cityCode;

    @ExcelProperty(value = "城市名称")
    private String cityName;

    @ExcelProperty(value = "区县编码")
    private String districtCode;

    @ExcelProperty(value = "区县名称")
    private String districtName;

    @ExcelProperty(value = "详细地址")
    private String detailAddress;

    @ExcelProperty(value = "是否默认")
    private Long isDefault;


}
