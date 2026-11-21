package org.dromara.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dis.api.RemoteDisService;
import org.dromara.user.domain.UserApply;
import org.dromara.user.domain.UserIdentityRel;
import org.dromara.user.domain.Userinfo;
import org.dromara.user.domain.bo.UserApplyAuditBo;
import org.dromara.user.domain.bo.UserApplyBo;
import org.dromara.user.domain.vo.UserApplyVo;
import org.dromara.user.mapper.UserApplyMapper;
import org.dromara.user.mapper.UserIdentityRelMapper;
import org.dromara.user.mapper.UserinfoMapper;
import org.dromara.user.service.IUserApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户申请审核记录Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserApplyServiceImpl implements IUserApplyService {

    private static final String IDENTITY_DIS = "dis";
    private static final Long AUDIT_PENDING = 0L;
    private static final Long AUDIT_PASS = 1L;
    private static final Long AUDIT_REJECT = 2L;
    private static final Long IDENTITY_ENABLED = 1L;

    private final UserApplyMapper baseMapper;
    private final UserIdentityRelMapper userIdentityRelMapper;
    private final UserinfoMapper userinfoMapper;

    @DubboReference
    private RemoteDisService remoteDisService;

    @Override
    public UserApplyVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<UserApplyVo> queryPageList(UserApplyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<UserApply> lqw = buildQueryWrapper(bo);
        Page<UserApplyVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<UserApplyVo> queryList(UserApplyBo bo) {
        LambdaQueryWrapper<UserApply> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<UserApply> buildQueryWrapper(UserApplyBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<UserApply> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(UserApply::getId);
        lqw.eq(bo.getUserinfoId() != null, UserApply::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyIdentityType()), UserApply::getApplyIdentityType, bo.getApplyIdentityType());
        lqw.eq(bo.getApplyType() != null, UserApply::getApplyType, bo.getApplyType());
        lqw.eq(bo.getUserType() != null, UserApply::getUserType, bo.getUserType());
        lqw.like(StringUtils.isNotBlank(bo.getCompanyName()), UserApply::getCompanyName, bo.getCompanyName());
        lqw.eq(StringUtils.isNotBlank(bo.getSocialCreditCode()), UserApply::getSocialCreditCode, bo.getSocialCreditCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessLicenseUrl()), UserApply::getBusinessLicenseUrl, bo.getBusinessLicenseUrl());
        lqw.like(StringUtils.isNotBlank(bo.getLegalPersonName()), UserApply::getLegalPersonName, bo.getLegalPersonName());
        lqw.eq(StringUtils.isNotBlank(bo.getLegalPersonIdCard()), UserApply::getLegalPersonIdCard, bo.getLegalPersonIdCard());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardFrontUrl()), UserApply::getIdCardFrontUrl, bo.getIdCardFrontUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardBackUrl()), UserApply::getIdCardBackUrl, bo.getIdCardBackUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getQualificationFiles()), UserApply::getQualificationFiles, bo.getQualificationFiles());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyRemark()), UserApply::getApplyRemark, bo.getApplyRemark());
        lqw.eq(bo.getAuditStatus() != null, UserApply::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditRemark()), UserApply::getAuditRemark, bo.getAuditRemark());
        lqw.eq(bo.getAuditUserId() != null, UserApply::getAuditUserId, bo.getAuditUserId());
        lqw.like(StringUtils.isNotBlank(bo.getAuditUserName()), UserApply::getAuditUserName, bo.getAuditUserName());
        lqw.eq(bo.getAssignedDeptId() != null, UserApply::getAssignedDeptId, bo.getAssignedDeptId());
        lqw.eq(bo.getAssignedSalesmanId() != null, UserApply::getAssignedSalesmanId, bo.getAssignedSalesmanId());
        lqw.eq(bo.getAuditTime() != null, UserApply::getAuditTime, bo.getAuditTime());
        return lqw;
    }

    @Override
    public Boolean insertByBo(UserApplyBo bo) {
        UserApply add = MapstructUtils.convert(bo, UserApply.class);
        if (add.getAuditStatus() == null) {
            add.setAuditStatus(AUDIT_PENDING);
        }
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(UserApplyBo bo) {
        UserApply update = MapstructUtils.convert(bo, UserApply.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(UserApply entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean audit(UserApplyAuditBo bo) {
        if (bo.getAuditStatus() == null
            || (!AUDIT_PASS.equals(bo.getAuditStatus()) && !AUDIT_REJECT.equals(bo.getAuditStatus()))) {
            throw new ServiceException("审核结果只能是通过或驳回");
        }
        UserApply apply = baseMapper.selectById(bo.getId());
        if (apply == null) {
            throw new ServiceException("申请单不存在");
        }
        if (!AUDIT_PENDING.equals(apply.getAuditStatus())) {
            throw new ServiceException("申请单已审核");
        }
        if (apply.getUserinfoId() == null) {
            throw new ServiceException("申请单缺少用户信息");
        }

        apply.setAuditStatus(bo.getAuditStatus());
        apply.setAuditRemark(bo.getAuditRemark());
        apply.setAuditUserId(LoginHelper.getUserId());
        apply.setAuditUserName(LoginHelper.getUsername());
        apply.setAuditTime(new Date());
        baseMapper.updateById(apply);
        if (!AUDIT_PASS.equals(bo.getAuditStatus())) {
            return true;
        }
        upsertIdentity(apply.getUserinfoId(), apply.getApplyIdentityType());
        if (IDENTITY_DIS.equalsIgnoreCase(apply.getApplyIdentityType())) {
            activateDistributor(apply.getUserinfoId());
        }
        return true;
    }

    private void upsertIdentity(Long userinfoId, String identityType) {
        if (userinfoId == null || StringUtils.isBlank(identityType)) {
            return;
        }
        UserIdentityRel exist = userIdentityRelMapper.selectOne(
            Wrappers.<UserIdentityRel>lambdaQuery()
                .eq(UserIdentityRel::getUserinfoId, userinfoId)
                .eq(UserIdentityRel::getIdentityType, identityType)
                .last("LIMIT 1")
        );
        if (exist == null) {
            UserIdentityRel rel = new UserIdentityRel();
            rel.setUserinfoId(userinfoId);
            rel.setIdentityType(identityType);
            rel.setStatus(IDENTITY_ENABLED);
            userIdentityRelMapper.insert(rel);
            return;
        }
        if (!IDENTITY_ENABLED.equals(exist.getStatus())) {
            exist.setStatus(IDENTITY_ENABLED);
            userIdentityRelMapper.updateById(exist);
        }
    }

    private void activateDistributor(Long userinfoId) {
        Userinfo userinfo = userinfoMapper.selectById(userinfoId);
        if (userinfo == null) {
            throw new ServiceException("用户信息不存在");
        }
        boolean changed = false;
        if (userinfo.getDistLevelId() == null) {
            Long defaultLevelId = remoteDisService.getDefaultLevelId();
            if (defaultLevelId != null) {
                userinfo.setDistLevelId(defaultLevelId);
                changed = true;
            }
        }
        if (StringUtils.isBlank(userinfo.getInviteCode())) {
            userinfo.setInviteCode(generateInviteCode());
            changed = true;
        }
        if (changed) {
            userinfoMapper.updateById(userinfo);
        }
    }

    private String generateInviteCode() {
        for (int i = 0; i < 8; i++) {
            String code = RandomUtil.randomStringUpper(8);
            Long cnt = userinfoMapper.selectCount(
                Wrappers.<Userinfo>lambdaQuery().eq(Userinfo::getInviteCode, code)
            );
            if (cnt == null || cnt == 0) {
                return code;
            }
        }
        throw new ServiceException("生成邀请码失败，请重试");
    }
}
