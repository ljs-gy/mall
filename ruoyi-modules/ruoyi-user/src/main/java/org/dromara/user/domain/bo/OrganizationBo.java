package org.dromara.user.domain.bo;

import org.dromara.user.domain.Organization;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 组织业务对象 um_organization
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Organization.class, reverseConvertGenerate = false)
public class OrganizationBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 组织编码
     */
    @NotBlank(message = "组织编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orgCode;

    /**
     * 组织名称
     */
    @NotBlank(message = "组织名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orgName;

    /**
     * 组织简称
     */
    private String orgShortName;

    /**
     * 上级组织代码（根节点公司填0）
     */
    @NotBlank(message = "上级组织代码（根节点公司填0）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String parentCode;

    /**
     * 组织类型：1-企业客户(公司根节点), 2-管理组织, 3-采购组织
     */
    @NotNull(message = "组织类型：1-企业客户(公司根节点), 2-管理组织, 3-采购组织不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotBlank(message = "状态（0正常 1停用）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;


}
