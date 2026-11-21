package org.dromara.user.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.dis.api.RemoteDisService;
import org.springframework.stereotype.Service;
import org.dromara.user.domain.bo.UserinfoBo;
import org.dromara.user.domain.vo.UserinfoVo;
import org.dromara.user.domain.Userinfo;
import org.dromara.user.mapper.UserinfoMapper;
import org.dromara.user.service.IUserinfoService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户信息Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserinfoServiceImpl implements IUserinfoService {

    private final UserinfoMapper baseMapper;

    @DubboReference
    private RemoteDisService remoteDisService;

    /**
     * 查询用户信息
     *
     * @param id 主键
     * @return 用户信息
     */
    @Override
    public UserinfoVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户信息分页列表
     */
    @Override
    public TableDataInfo<UserinfoVo> queryPageList(UserinfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Userinfo> lqw = buildQueryWrapper(bo);
        Page<UserinfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户信息列表
     *
     * @param bo 查询条件
     * @return 用户信息列表
     */
    @Override
    public List<UserinfoVo> queryList(UserinfoBo bo) {
        LambdaQueryWrapper<Userinfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Userinfo> buildQueryWrapper(UserinfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Userinfo> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Userinfo::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getUserinfoCode()), Userinfo::getUserinfoCode, bo.getUserinfoCode());
        lqw.like(StringUtils.isNotBlank(bo.getUserinfoName()), Userinfo::getUserinfoName, bo.getUserinfoName());
        lqw.like(StringUtils.isNotBlank(bo.getUserinfoShortName()), Userinfo::getUserinfoShortName, bo.getUserinfoShortName());
        lqw.eq(StringUtils.isNotBlank(bo.getUserinfoLogoUrl()), Userinfo::getUserinfoLogoUrl, bo.getUserinfoLogoUrl());
        lqw.eq(bo.getUserinfoType() != null, Userinfo::getUserinfoType, bo.getUserinfoType());
        lqw.eq(StringUtils.isNotBlank(bo.getInvitationCode()), Userinfo::getInvitationCode, bo.getInvitationCode());
        lqw.eq(StringUtils.isNotBlank(bo.getInviteCode()), Userinfo::getInviteCode, bo.getInviteCode());
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), Userinfo::getContactName, bo.getContactName());
        lqw.eq(StringUtils.isNotBlank(bo.getContactPhone()), Userinfo::getContactPhone, bo.getContactPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getContactEmail()), Userinfo::getContactEmail, bo.getContactEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getSocialCreditCode()), Userinfo::getSocialCreditCode, bo.getSocialCreditCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessLicenseNo()), Userinfo::getBusinessLicenseNo, bo.getBusinessLicenseNo());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessLicenseUrl()), Userinfo::getBusinessLicenseUrl, bo.getBusinessLicenseUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessLicenseAddress()), Userinfo::getBusinessLicenseAddress, bo.getBusinessLicenseAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessScope()), Userinfo::getBusinessScope, bo.getBusinessScope());
        lqw.eq(bo.getBusinessTermStart() != null, Userinfo::getBusinessTermStart, bo.getBusinessTermStart());
        lqw.eq(bo.getBusinessTermEnd() != null, Userinfo::getBusinessTermEnd, bo.getBusinessTermEnd());
        lqw.eq(StringUtils.isNotBlank(bo.getQualificationFiles()), Userinfo::getQualificationFiles, bo.getQualificationFiles());
        lqw.eq(StringUtils.isNotBlank(bo.getThreeInOneUrl()), Userinfo::getThreeInOneUrl, bo.getThreeInOneUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getTaxNo()), Userinfo::getTaxNo, bo.getTaxNo());
        lqw.eq(StringUtils.isNotBlank(bo.getInvoicePrintAddress()), Userinfo::getInvoicePrintAddress, bo.getInvoicePrintAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getInvoicePrintPhone()), Userinfo::getInvoicePrintPhone, bo.getInvoicePrintPhone());
        lqw.like(StringUtils.isNotBlank(bo.getBankName()), Userinfo::getBankName, bo.getBankName());
        lqw.eq(StringUtils.isNotBlank(bo.getBankAccount()), Userinfo::getBankAccount, bo.getBankAccount());
        lqw.eq(StringUtils.isNotBlank(bo.getBankLinkCode()), Userinfo::getBankLinkCode, bo.getBankLinkCode());
        lqw.eq(StringUtils.isNotBlank(bo.getLegalPerson()), Userinfo::getLegalPerson, bo.getLegalPerson());
        lqw.eq(bo.getRegisteredCapital() != null, Userinfo::getRegisteredCapital, bo.getRegisteredCapital());
        lqw.eq(StringUtils.isNotBlank(bo.getCompanyProfile()), Userinfo::getCompanyProfile, bo.getCompanyProfile());
        lqw.eq(StringUtils.isNotBlank(bo.getSellerContractUrl()), Userinfo::getSellerContractUrl, bo.getSellerContractUrl());
        lqw.like(StringUtils.isNotBlank(bo.getTrusteeName()), Userinfo::getTrusteeName, bo.getTrusteeName());
        lqw.eq(StringUtils.isNotBlank(bo.getAuthLetterUrl()), Userinfo::getAuthLetterUrl, bo.getAuthLetterUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getAuthIdCardFront()), Userinfo::getAuthIdCardFront, bo.getAuthIdCardFront());
        lqw.eq(StringUtils.isNotBlank(bo.getAuthIdCardBack()), Userinfo::getAuthIdCardBack, bo.getAuthIdCardBack());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardNo()), Userinfo::getIdCardNo, bo.getIdCardNo());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardFrontUrl()), Userinfo::getIdCardFrontUrl, bo.getIdCardFrontUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardBackUrl()), Userinfo::getIdCardBackUrl, bo.getIdCardBackUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceCode()), Userinfo::getProvinceCode, bo.getProvinceCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCityCode()), Userinfo::getCityCode, bo.getCityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getDistrictCode()), Userinfo::getDistrictCode, bo.getDistrictCode());
        lqw.eq(StringUtils.isNotBlank(bo.getDetailAddress()), Userinfo::getDetailAddress, bo.getDetailAddress());
        lqw.eq(bo.getSalesmanDeptId() != null, Userinfo::getSalesmanDeptId, bo.getSalesmanDeptId());
        lqw.eq(StringUtils.isNotBlank(bo.getSalesmanEmpCode()), Userinfo::getSalesmanEmpCode, bo.getSalesmanEmpCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSalesmanOrgCode()), Userinfo::getSalesmanOrgCode, bo.getSalesmanOrgCode());
        lqw.eq(bo.getDistLevelId() != null, Userinfo::getDistLevelId, bo.getDistLevelId());
        lqw.eq(bo.getIsBlacklisted() != null, Userinfo::getIsBlacklisted, bo.getIsBlacklisted());
        lqw.eq(StringUtils.isNotBlank(bo.getBlacklistReason()), Userinfo::getBlacklistReason, bo.getBlacklistReason());
        lqw.eq(bo.getAuditStatus() != null, Userinfo::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditRemark()), Userinfo::getAuditRemark, bo.getAuditRemark());
        return lqw;
    }

    /**
     * 新增用户信息
     *
     * @param bo 用户信息
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(UserinfoBo bo) {
        Userinfo add = MapstructUtils.convert(bo, Userinfo.class);
        if (StringUtils.isBlank(add.getUserinfoCode())) {
            add.setUserinfoCode(IdGeneratorUtil.nextIdWithPrefix("UI"));
        }
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            bo.setUserinfoCode(add.getUserinfoCode());
            // invitationCode = 邀请人的分享码 → 绑定上下级
            if (StringUtils.isNotBlank(add.getInvitationCode())) {
                remoteDisService.bindByInviteCode(add.getId(), add.getInvitationCode());
            }
        }
        return flag;
    }

    /**
     * 修改用户信息
     *
     * @param bo 用户信息
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(UserinfoBo bo) {
        Userinfo update = MapstructUtils.convert(bo, Userinfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Userinfo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
