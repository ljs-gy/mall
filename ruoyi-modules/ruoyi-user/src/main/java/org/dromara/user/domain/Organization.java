package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 组织对象 um_organization
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_organization")
public class Organization extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织简称
     */
    private String orgShortName;

    /**
     * 上级组织代码（根节点公司填0）
     */
    private String parentCode;

    /**
     * 组织类型：1-企业客户(公司根节点), 2-管理组织, 3-采购组织
     */
    private Long orgType;

    /**
     * 证件类型：1企业营业执照 2事业单位法人证书 3社会统一社会信用代码 4社会团体登记证书 5居民身份证
     */
    private Long certType;

    /**
     * 证件号
     */
    private String certNo;

    /**
     * 营业执照照片
     */
    private String businessLicenseUrl;

    /**
     * 管理员名称
     */
    private String adminName;

    /**
     * 管理员手机号
     */
    private String adminPhone;

    /**
     * 管理员密码
     */
    private String adminPassword;

    /**
     * 管理员证件类型：1居民身份证 2护照 3港澳居民来往内地通行证 4台湾居民来往内地通行证
     */
    private Long adminCertType;

    /**
     * 管理员证件号
     */
    private String adminCertNo;

    /**
     * 公司联系电话
     */
    private String contactPhone;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;


}
