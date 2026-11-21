package org.dromara.user.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.user.domain.bo.OrganizationBo;
import org.dromara.user.domain.vo.OrganizationVo;
import org.dromara.user.domain.Organization;
import org.dromara.user.mapper.OrganizationMapper;
import org.dromara.user.service.IOrganizationService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 组织Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrganizationServiceImpl implements IOrganizationService {

    private final OrganizationMapper baseMapper;

    /**
     * 查询组织
     *
     * @param id 主键
     * @return 组织
     */
    @Override
    public OrganizationVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询组织列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 组织分页列表
     */
    @Override
    public TableDataInfo<OrganizationVo> queryPageList(OrganizationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Organization> lqw = buildQueryWrapper(bo);
        Page<OrganizationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的组织列表
     *
     * @param bo 查询条件
     * @return 组织列表
     */
    @Override
    public List<OrganizationVo> queryList(OrganizationBo bo) {
        LambdaQueryWrapper<Organization> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Organization> buildQueryWrapper(OrganizationBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Organization> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Organization::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrgCode()), Organization::getOrgCode, bo.getOrgCode());
        lqw.like(StringUtils.isNotBlank(bo.getOrgName()), Organization::getOrgName, bo.getOrgName());
        lqw.like(StringUtils.isNotBlank(bo.getOrgShortName()), Organization::getOrgShortName, bo.getOrgShortName());
        lqw.eq(StringUtils.isNotBlank(bo.getParentCode()), Organization::getParentCode, bo.getParentCode());
        lqw.eq(bo.getOrgType() != null, Organization::getOrgType, bo.getOrgType());
        lqw.eq(bo.getCertType() != null, Organization::getCertType, bo.getCertType());
        lqw.eq(StringUtils.isNotBlank(bo.getCertNo()), Organization::getCertNo, bo.getCertNo());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessLicenseUrl()), Organization::getBusinessLicenseUrl, bo.getBusinessLicenseUrl());
        lqw.like(StringUtils.isNotBlank(bo.getAdminName()), Organization::getAdminName, bo.getAdminName());
        lqw.eq(StringUtils.isNotBlank(bo.getAdminPhone()), Organization::getAdminPhone, bo.getAdminPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getAdminPassword()), Organization::getAdminPassword, bo.getAdminPassword());
        lqw.eq(bo.getAdminCertType() != null, Organization::getAdminCertType, bo.getAdminCertType());
        lqw.eq(StringUtils.isNotBlank(bo.getAdminCertNo()), Organization::getAdminCertNo, bo.getAdminCertNo());
        lqw.eq(StringUtils.isNotBlank(bo.getContactPhone()), Organization::getContactPhone, bo.getContactPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Organization::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增组织
     *
     * @param bo 组织
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(OrganizationBo bo) {
        Organization add = MapstructUtils.convert(bo, Organization.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改组织
     *
     * @param bo 组织
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(OrganizationBo bo) {
        Organization update = MapstructUtils.convert(bo, Organization.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Organization entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除组织信息
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
