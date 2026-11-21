package org.dromara.user.domain.vo;

import org.dromara.user.domain.UserCategory;
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
 * 用户经营类目资质视图对象 um_user_category
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = UserCategory.class)
public class UserCategoryVo implements Serializable {

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
     * 业务类型：merchant(商家售卖类目), supplier(供应商供货类目)
     */
    @ExcelProperty(value = "业务类型：merchant(商家售卖类目), supplier(供应商供货类目)")
    private String bizType;

    /**
     * 商品平台类目ID
     */
    @ExcelProperty(value = "商品平台类目ID")
    private Long categoryId;

    /**
     * 审核状态：0待审核，1已通过，2已驳回
     */
    @ExcelProperty(value = "审核状态：0待审核，1已通过，2已驳回")
    private Long auditStatus;


}
