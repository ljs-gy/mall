package org.dromara.pm.service.impl;

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
import org.dromara.pm.domain.bo.PromoScopeUserBo;
import org.dromara.pm.domain.vo.PromoScopeUserVo;
import org.dromara.pm.domain.PromoScopeUser;
import org.dromara.pm.mapper.PromoScopeUserMapper;
import org.dromara.pm.service.IPromoScopeUserService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 营销活动与优惠券人群关系Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PromoScopeUserServiceImpl implements IPromoScopeUserService {

    private final PromoScopeUserMapper baseMapper;

    /**
     * 查询营销活动与优惠券人群关系
     *
     * @param id 主键
     * @return 营销活动与优惠券人群关系
     */
    @Override
    public PromoScopeUserVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询营销活动与优惠券人群关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 营销活动与优惠券人群关系分页列表
     */
    @Override
    public TableDataInfo<PromoScopeUserVo> queryPageList(PromoScopeUserBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PromoScopeUser> lqw = buildQueryWrapper(bo);
        Page<PromoScopeUserVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的营销活动与优惠券人群关系列表
     *
     * @param bo 查询条件
     * @return 营销活动与优惠券人群关系列表
     */
    @Override
    public List<PromoScopeUserVo> queryList(PromoScopeUserBo bo) {
        LambdaQueryWrapper<PromoScopeUser> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PromoScopeUser> buildQueryWrapper(PromoScopeUserBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<PromoScopeUser> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(PromoScopeUser::getId);
        lqw.eq(bo.getSourceType() != null, PromoScopeUser::getSourceType, bo.getSourceType());
        lqw.eq(bo.getSourceId() != null, PromoScopeUser::getSourceId, bo.getSourceId());
        lqw.eq(bo.getTargetId() != null, PromoScopeUser::getTargetId, bo.getTargetId());
        return lqw;
    }

    /**
     * 新增营销活动与优惠券人群关系
     *
     * @param bo 营销活动与优惠券人群关系
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PromoScopeUserBo bo) {
        PromoScopeUser add = MapstructUtils.convert(bo, PromoScopeUser.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改营销活动与优惠券人群关系
     *
     * @param bo 营销活动与优惠券人群关系
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PromoScopeUserBo bo) {
        PromoScopeUser update = MapstructUtils.convert(bo, PromoScopeUser.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(PromoScopeUser entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除营销活动与优惠券人群关系信息
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
