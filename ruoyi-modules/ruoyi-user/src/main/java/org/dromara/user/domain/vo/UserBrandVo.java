package org.dromara.user.domain.vo;

import org.dromara.user.domain.UserBrand;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 用户经营品牌资质视图对象 um_user_brand
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = UserBrand.class)
public class UserBrandVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 用户信息ID
     */
    @ExcelProperty(value = "用户信息ID")
    private Long userinfoId;

    /**
     * 业务类型：merchant(商家经营品牌), supplier(供应商供货品牌)
     */
    @ExcelProperty(value = "业务类型：merchant(商家经营品牌), supplier(供应商供货品牌)")
    private String bizType;

    /**
     * 品牌ID
     */
    @ExcelProperty(value = "品牌ID")
    private Long brandId;

    /**
     * 品牌授权书图片
     */
    @ExcelProperty(value = "品牌授权书图片")
    private String authLetterUrl;

    /**
     * 审核状态：0待审核，1已通过，2已驳回
     */
    @ExcelProperty(value = "审核状态：0待审核，1已通过，2已驳回")
    private Long auditStatus;


}
