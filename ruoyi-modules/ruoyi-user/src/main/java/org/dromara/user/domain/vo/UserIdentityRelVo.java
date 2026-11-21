package org.dromara.user.domain.vo;

import org.dromara.user.domain.UserIdentityRel;
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
 * 用户主体身份关系视图对象 um_user_identity_rel
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = UserIdentityRel.class)
public class UserIdentityRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 用户信息ID(um_userinfo.id)
     */
    @ExcelProperty(value = "用户信息ID(um_userinfo.id)")
    private Long userinfoId;

    /**
     * 身份类型(dealer:经销商, merchant:商家, plat:平台, buy:采购商, supplier:供应商, dis:分销员)
     */
    @ExcelProperty(value = "身份类型(dealer:经销商, merchant:商家, plat:平台, buy:采购商, supplier:供应商, dis:分销员)")
    private String identityType;

    /**
     * 身份状态(1:启用 0:禁用)
     */
    @ExcelProperty(value = "身份状态(1:启用 0:禁用)")
    private Long status;


}
