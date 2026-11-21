package org.dromara.user.domain.vo;

import org.dromara.user.domain.Store;
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
 * 商家店铺视图对象 um_store
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Store.class)
public class StoreVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 店铺ID
     */
    @ExcelProperty(value = "店铺ID")
    private Long id;

    /**
     * 所属用户信息ID
     */
    @ExcelProperty(value = "所属用户信息ID")
    private Long userinfoId;

    /**
     * 店铺唯一编号
     */
    @ExcelProperty(value = "店铺唯一编号")
    private String storeNo;

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "店铺名称")
    private String storeName;

    /**
     * 店铺Logo
     */
    @ExcelProperty(value = "店铺Logo")
    private String storeLogoUrl;

    /**
     * 店铺简介
     */
    @ExcelProperty(value = "店铺简介")
    private String storeIntro;

    /**
     * 店铺状态：1正常，0关闭
     */
    @ExcelProperty(value = "店铺状态：1正常，0关闭")
    private Long status;


}
