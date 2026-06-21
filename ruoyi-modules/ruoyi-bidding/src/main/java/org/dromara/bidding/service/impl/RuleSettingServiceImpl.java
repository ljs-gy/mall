package org.dromara.bidding.service.impl;

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
import org.dromara.bidding.domain.bo.RuleSettingBo;
import org.dromara.bidding.domain.vo.RuleSettingVo;
import org.dromara.bidding.domain.RuleSetting;
import org.dromara.bidding.mapper.RuleSettingMapper;
import org.dromara.bidding.service.IRuleSettingService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 竞价规则Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleSettingServiceImpl implements IRuleSettingService {

    private final RuleSettingMapper baseMapper;

    @Override
    public RuleSettingVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<RuleSettingVo> queryPageList(RuleSettingBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<RuleSetting> lqw = buildQueryWrapper(bo);
        Page<RuleSettingVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<RuleSettingVo> queryList(RuleSettingBo bo) {
        LambdaQueryWrapper<RuleSetting> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<RuleSetting> buildQueryWrapper(RuleSettingBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<RuleSetting> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getRuleName()), RuleSetting::getRuleName, bo.getRuleName());
        lqw.eq(bo.getBidType() != null, RuleSetting::getBidType, bo.getBidType());
        lqw.eq(bo.getBidModel() != null, RuleSetting::getBidModel, bo.getBidModel());
        lqw.eq(bo.getCommissionType() != null, RuleSetting::getCommissionType, bo.getCommissionType());
        return lqw;
    }

    @Override
    public Boolean insertByBo(RuleSettingBo bo) {
        RuleSetting add = MapstructUtils.convert(bo, RuleSetting.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(RuleSettingBo bo) {
        RuleSetting update = MapstructUtils.convert(bo, RuleSetting.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(RuleSetting entity){
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
