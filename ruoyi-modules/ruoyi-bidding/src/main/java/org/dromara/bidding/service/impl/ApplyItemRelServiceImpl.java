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
import org.dromara.bidding.domain.bo.ApplyItemRelBo;
import org.dromara.bidding.domain.vo.ApplyItemRelVo;
import org.dromara.bidding.domain.ApplyItemRel;
import org.dromara.bidding.mapper.ApplyItemRelMapper;
import org.dromara.bidding.service.IApplyItemRelService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 报名标的关联Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ApplyItemRelServiceImpl implements IApplyItemRelService {

    private final ApplyItemRelMapper baseMapper;

    @Override
    public ApplyItemRelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ApplyItemRelVo> queryPageList(ApplyItemRelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ApplyItemRel> lqw = buildQueryWrapper(bo);
        Page<ApplyItemRelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<ApplyItemRelVo> queryList(ApplyItemRelBo bo) {
        LambdaQueryWrapper<ApplyItemRel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ApplyItemRel> buildQueryWrapper(ApplyItemRelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ApplyItemRel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ApplyItemRel::getId);
        lqw.eq(bo.getApplyId() != null, ApplyItemRel::getApplyId, bo.getApplyId());
        lqw.eq(bo.getItemId() != null, ApplyItemRel::getItemId, bo.getItemId());
        return lqw;
    }

    @Override
    public Boolean insertByBo(ApplyItemRelBo bo) {
        ApplyItemRel add = MapstructUtils.convert(bo, ApplyItemRel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(ApplyItemRelBo bo) {
        ApplyItemRel update = MapstructUtils.convert(bo, ApplyItemRel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(ApplyItemRel entity){
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
