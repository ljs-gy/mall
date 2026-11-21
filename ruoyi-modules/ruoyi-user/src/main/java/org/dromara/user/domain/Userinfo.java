package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 用户信息对象 um_userinfo
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_userinfo")
public class Userinfo extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 用户信息业务编码（买家/卖家主体码）
     */
    private String userinfoCode;

    /**
     * 用户名称
     */
    private String userinfoName;

    /**
     * 商家简称
     */
    private String userinfoShortName;

    /**
     * 商户LOGO
     */
    private String userinfoLogoUrl;

    /**
     * 用户类型：1个人，2企业
     */
    private Long userinfoType;

    /**
     * 注册时填写的邀请人邀请码
     */
    private String invitationCode;

    /**
     * 我的分享邀请码（分销员审核通过后生成）
     */
    private String inviteCode;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 统一社会信用代码
     */
    private String socialCreditCode;

    /**
     * 营业执照编号
     */
    private String businessLicenseNo;

    /**
     * 营业执照照片
     */
    private String businessLicenseUrl;

    /**
     * 营业执照地址
     */
    private String businessLicenseAddress;

    /**
     * 营业范围
     */
    private String businessScope;

    /**
     * 营业开始日期
     */
    private Date businessTermStart;

    /**
     * 营业结束日期
     */
    private Date businessTermEnd;

    /**
     * 资质文件
     */
    private String qualificationFiles;

    /**
     * 三证合一照片
     */
    private String threeInOneUrl;

    /**
     * 公司税号
     */
    private String taxNo;

    /**
     * 发票打印地址
     */
    private String invoicePrintAddress;

    /**
     * 发票打印电话
     */
    private String invoicePrintPhone;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 开户银行账号
     */
    private String bankAccount;

    /**
     * 银行联行号
     */
    private String bankLinkCode;

    /**
     * 法人姓名
     */
    private String legalPerson;

    /**
     * 注册资金
     */
    private BigDecimal registeredCapital;

    /**
     * 公司简介
     */
    private String companyProfile;

    /**
     * 卖家协议
     */
    private String sellerContractUrl;

    /**
     * 授权人姓名
     */
    private String trusteeName;

    /**
     * 授权委托书
     */
    private String authLetterUrl;

    /**
     * 授权人身份证正面
     */
    private String authIdCardFront;

    /**
     * 授权人身份证反面
     */
    private String authIdCardBack;

    /**
     * 身份证号（个人分销资质）
     */
    private String idCardNo;

    /**
     * 个人身份证正面
     */
    private String idCardFrontUrl;

    /**
     * 个人身份证反面
     */
    private String idCardBackUrl;

    /**
     * 省编码
     */
    private String provinceCode;

    /**
     * 市编码
     */
    private String cityCode;

    /**
     * 区编码
     */
    private String districtCode;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 归属业务部门ID
     */
    private Long salesmanDeptId;

    /**
     * 归属业务员编码
     */
    private String salesmanEmpCode;

    /**
     * 归属业务组织编码
     */
    private String salesmanOrgCode;

    /**
     * 分销员等级ID
     */
    private Long distLevelId;

    /**
     * 是否黑名单：0正常，1黑名单
     */
    private Long isBlacklisted;

    /**
     * 加黑原因
     */
    private String blacklistReason;

    /**
     * 审核状态：0待提交，1待审核，2审核通过，3审核驳回
     */
    private Long auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注信息
     */
    private String remark;


}
