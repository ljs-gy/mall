package org.dromara.dis.service.impl;

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
import org.dromara.dis.domain.bo.DistSettingBo;
import org.dromara.dis.domain.vo.DistSettingVo;
import org.dromara.dis.domain.DistSetting;
import org.dromara.dis.mapper.DistSettingMapper;
import org.dromara.dis.service.IDistSettingService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 分销基础配置Service业务层处理
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistSettingServiceImpl implements IDistSettingService {

    private final DistSettingMapper baseMapper;

    /**
     * 查询分销基础配置
     *
     * @param id 主键
     * @return 分销基础配置
     */
    @Override
    public DistSettingVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分销基础配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销基础配置分页列表
     */
    @Override
    public TableDataInfo<DistSettingVo> queryPageList(DistSettingBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DistSetting> lqw = buildQueryWrapper(bo);
        Page<DistSettingVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分销基础配置列表
     *
     * @param bo 查询条件
     * @return 分销基础配置列表
     */
    @Override
    public List<DistSettingVo> queryList(DistSettingBo bo) {
        LambdaQueryWrapper<DistSetting> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DistSetting> buildQueryWrapper(DistSettingBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DistSetting> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(DistSetting::getId);
        lqw.eq(bo.getBaseFirstRate() != null, DistSetting::getBaseFirstRate, bo.getBaseFirstRate());
        lqw.eq(bo.getBaseSecondRate() != null, DistSetting::getBaseSecondRate, bo.getBaseSecondRate());
        lqw.eq(bo.getBaseThirdRate() != null, DistSetting::getBaseThirdRate, bo.getBaseThirdRate());
        lqw.eq(bo.getQualifyType() != null, DistSetting::getQualifyType, bo.getQualifyType());
        lqw.eq(bo.getIsBuyerHasCommission() != null, DistSetting::getIsBuyerHasCommission, bo.getIsBuyerHasCommission());
        lqw.eq(StringUtils.isNotBlank(bo.getJoinBgUrl()), DistSetting::getJoinBgUrl, bo.getJoinBgUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getInviteBgUrl()), DistSetting::getInviteBgUrl, bo.getInviteBgUrl());
        return lqw;
    }

    /**
     * 新增分销基础配置
     *
     * @param bo 分销基础配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DistSettingBo bo) {
        DistSetting add = MapstructUtils.convert(bo, DistSetting.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分销基础配置
     *
     * @param bo 分销基础配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DistSettingBo bo) {
        DistSetting update = MapstructUtils.convert(bo, DistSetting.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DistSetting entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分销基础配置信息
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
