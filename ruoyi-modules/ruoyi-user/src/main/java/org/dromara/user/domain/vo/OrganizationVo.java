package org.dromara.user.domain.vo;

import org.dromara.user.domain.Organization;
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
 * 组织视图对象 um_organization
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Organization.class)
public class OrganizationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 组织编码
     */
    @ExcelProperty(value = "组织编码")
    private String orgCode;

    /**
     * 组织名称
     */
    @ExcelProperty(value = "组织名称")
    private String orgName;

    /**
     * 组织简称
     */
    @ExcelProperty(value = "组织简称")
    private String orgShortName;

    /**
     * 上级组织代码（根节点公司填0）
     */
    @ExcelProperty(value = "上级组织代码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "根=节点公司填0")
    private String parentCode;

    /**
     * 组织类型：1-企业客户(公司根节点), 2-管理组织, 3-采购组织
     */
    @ExcelProperty(value = "组织类型：1-企业客户(公司根节点), 2-管理组织, 3-采购组织")
    private Long orgType;

    /**
     * 证件类型：1企业营业执照 2事业单位法人证书 3社会统一社会信用代码 4社会团体登记证书 5居民身份证
     */
    @ExcelProperty(value = "证件类型：1企业营业执照 2事业单位法人证书 3社会统一社会信用代码 4社会团体登记证书 5居民身份证")
    private Long certType;

    /**
     * 证件号
     */
    @ExcelProperty(value = "证件号")
    private String certNo;

    /**
     * 营业执照照片
     */
    @ExcelProperty(value = "营业执照照片")
    private String businessLicenseUrl;

    /**
     * 管理员名称
     */
    @ExcelProperty(value = "管理员名称")
    private String adminName;

    /**
     * 管理员手机号
     */
    @ExcelProperty(value = "管理员手机号")
    private String adminPhone;

    /**
     * 管理员密码
     */
    @ExcelProperty(value = "管理员密码")
    private String adminPassword;

    /**
     * 管理员证件类型：1居民身份证 2护照 3港澳居民来往内地通行证 4台湾居民来往内地通行证
     */
    @ExcelProperty(value = "管理员证件类型：1居民身份证 2护照 3港澳居民来往内地通行证 4台湾居民来往内地通行证")
    private Long adminCertType;

    /**
     * 管理员证件号
     */
    @ExcelProperty(value = "管理员证件号")
    private String adminCertNo;

    /**
     * 公司联系电话
     */
    @ExcelProperty(value = "公司联系电话")
    private String contactPhone;

    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;


}
