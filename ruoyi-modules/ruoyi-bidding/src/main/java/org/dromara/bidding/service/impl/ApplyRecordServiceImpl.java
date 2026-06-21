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
import org.dromara.bidding.domain.bo.ApplyRecordBo;
import org.dromara.bidding.domain.vo.ApplyRecordVo;
import org.dromara.bidding.domain.ApplyRecord;
import org.dromara.bidding.mapper.ApplyRecordMapper;
import org.dromara.bidding.service.IApplyRecordService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 报名记录Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ApplyRecordServiceImpl implements IApplyRecordService {

    private final ApplyRecordMapper baseMapper;

    @Override
    public ApplyRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ApplyRecordVo> queryPageList(ApplyRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ApplyRecord> lqw = buildQueryWrapper(bo);
        Page<ApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<ApplyRecordVo> queryList(ApplyRecordBo bo) {
        LambdaQueryWrapper<ApplyRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ApplyRecord> buildQueryWrapper(ApplyRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ApplyRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getApplyNo()), ApplyRecord::getApplyNo, bo.getApplyNo());
        lqw.eq(bo.getSessionId() != null, ApplyRecord::getSessionId, bo.getSessionId());
        lqw.eq(bo.getUserId() != null, ApplyRecord::getUserId, bo.getUserId());
        lqw.eq(bo.getAuditStatus() != null, ApplyRecord::getAuditStatus, bo.getAuditStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(ApplyRecordBo bo) {
        ApplyRecord add = MapstructUtils.convert(bo, ApplyRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(ApplyRecordBo bo) {
        ApplyRecord update = MapstructUtils.convert(bo, ApplyRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(ApplyRecord entity){
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
