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
import org.dromara.pm.domain.bo.PromoScopeTargetBo;
import org.dromara.pm.domain.vo.PromoScopeTargetVo;
import org.dromara.pm.domain.PromoScopeTarget;
import org.dromara.pm.mapper.PromoScopeTargetMapper;
import org.dromara.pm.service.IPromoScopeTargetService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 营销范围Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PromoScopeTargetServiceImpl implements IPromoScopeTargetService {

    private final PromoScopeTargetMapper baseMapper;

    /**
     * 查询营销范围
     *
     * @param id 主键
     * @return 营销范围
     */
    @Override
    public PromoScopeTargetVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询营销范围列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 营销范围分页列表
     */
    @Override
    public TableDataInfo<PromoScopeTargetVo> queryPageList(PromoScopeTargetBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PromoScopeTarget> lqw = buildQueryWrapper(bo);
        Page<PromoScopeTargetVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的营销范围列表
     *
     * @param bo 查询条件
     * @return 营销范围列表
     */
    @Override
    public List<PromoScopeTargetVo> queryList(PromoScopeTargetBo bo) {
        LambdaQueryWrapper<PromoScopeTarget> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PromoScopeTarget> buildQueryWrapper(PromoScopeTargetBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<PromoScopeTarget> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(PromoScopeTarget::getId);
        lqw.eq(bo.getSourceType() != null, PromoScopeTarget::getSourceType, bo.getSourceType());
        lqw.eq(bo.getSourceId() != null, PromoScopeTarget::getSourceId, bo.getSourceId());
        lqw.eq(bo.getTargetCategory() != null, PromoScopeTarget::getTargetCategory, bo.getTargetCategory());
        lqw.eq(bo.getTargetId() != null, PromoScopeTarget::getTargetId, bo.getTargetId());
        return lqw;
    }

    /**
     * 新增营销范围
     *
     * @param bo 营销范围
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PromoScopeTargetBo bo) {
        PromoScopeTarget add = MapstructUtils.convert(bo, PromoScopeTarget.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改营销范围
     *
     * @param bo 营销范围
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PromoScopeTargetBo bo) {
        PromoScopeTarget update = MapstructUtils.convert(bo, PromoScopeTarget.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(PromoScopeTarget entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除营销范围信息
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
