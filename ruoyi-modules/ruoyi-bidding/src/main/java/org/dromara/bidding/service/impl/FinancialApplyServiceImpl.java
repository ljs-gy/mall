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
import org.dromara.bidding.domain.bo.FinancialApplyBo;
import org.dromara.bidding.domain.vo.FinancialApplyVo;
import org.dromara.bidding.domain.FinancialApply;
import org.dromara.bidding.mapper.FinancialApplyMapper;
import org.dromara.bidding.service.IFinancialApplyService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 清算申请Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FinancialApplyServiceImpl implements IFinancialApplyService {

    private final FinancialApplyMapper baseMapper;

    @Override
    public FinancialApplyVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<FinancialApplyVo> queryPageList(FinancialApplyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FinancialApply> lqw = buildQueryWrapper(bo);
        Page<FinancialApplyVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<FinancialApplyVo> queryList(FinancialApplyBo bo) {
        LambdaQueryWrapper<FinancialApply> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<FinancialApply> buildQueryWrapper(FinancialApplyBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<FinancialApply> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getApplyNo()), FinancialApply::getApplyNo, bo.getApplyNo());
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), FinancialApply::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getUserId() != null, FinancialApply::getUserId, bo.getUserId());
        lqw.eq(bo.getApplyType() != null, FinancialApply::getApplyType, bo.getApplyType());
        lqw.eq(bo.getAuditStatus() != null, FinancialApply::getAuditStatus, bo.getAuditStatus());
        lqw.eq(bo.getExecutePayStatus() != null, FinancialApply::getExecutePayStatus, bo.getExecutePayStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(FinancialApplyBo bo) {
        FinancialApply add = MapstructUtils.convert(bo, FinancialApply.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(FinancialApplyBo bo) {
        FinancialApply update = MapstructUtils.convert(bo, FinancialApply.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(FinancialApply entity){
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
