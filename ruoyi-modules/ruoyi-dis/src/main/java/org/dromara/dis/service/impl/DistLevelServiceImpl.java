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
import org.dromara.dis.domain.bo.DistLevelBo;
import org.dromara.dis.domain.vo.DistLevelVo;
import org.dromara.dis.domain.DistLevel;
import org.dromara.dis.mapper.DistLevelMapper;
import org.dromara.dis.service.IDistLevelService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 分销员等级Service业务层处理
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistLevelServiceImpl implements IDistLevelService {

    private final DistLevelMapper baseMapper;

    /**
     * 查询分销员等级
     *
     * @param id 主键
     * @return 分销员等级
     */
    @Override
    public DistLevelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分销员等级列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销员等级分页列表
     */
    @Override
    public TableDataInfo<DistLevelVo> queryPageList(DistLevelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DistLevel> lqw = buildQueryWrapper(bo);
        Page<DistLevelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分销员等级列表
     *
     * @param bo 查询条件
     * @return 分销员等级列表
     */
    @Override
    public List<DistLevelVo> queryList(DistLevelBo bo) {
        LambdaQueryWrapper<DistLevel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DistLevel> buildQueryWrapper(DistLevelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DistLevel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(DistLevel::getId);
        lqw.like(StringUtils.isNotBlank(bo.getLevelName()), DistLevel::getLevelName, bo.getLevelName());
        lqw.eq(bo.getUpgradeSelfAmount() != null, DistLevel::getUpgradeSelfAmount, bo.getUpgradeSelfAmount());
        lqw.eq(bo.getUpgradeInviteCount() != null, DistLevel::getUpgradeInviteCount, bo.getUpgradeInviteCount());
        lqw.eq(bo.getUpgradeTotalCommission() != null, DistLevel::getUpgradeTotalCommission, bo.getUpgradeTotalCommission());
        lqw.eq(bo.getLevelWeight() != null, DistLevel::getLevelWeight, bo.getLevelWeight());
        lqw.eq(bo.getFirstRate() != null, DistLevel::getFirstRate, bo.getFirstRate());
        lqw.eq(bo.getSecondRate() != null, DistLevel::getSecondRate, bo.getSecondRate());
        lqw.eq(bo.getThirdRate() != null, DistLevel::getThirdRate, bo.getThirdRate());
        return lqw;
    }

    /**
     * 新增分销员等级
     *
     * @param bo 分销员等级
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DistLevelBo bo) {
        DistLevel add = MapstructUtils.convert(bo, DistLevel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分销员等级
     *
     * @param bo 分销员等级
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DistLevelBo bo) {
        DistLevel update = MapstructUtils.convert(bo, DistLevel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DistLevel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分销员等级信息
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
