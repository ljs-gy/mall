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
import org.dromara.bidding.domain.bo.BidLogBo;
import org.dromara.bidding.domain.vo.BidLogVo;
import org.dromara.bidding.domain.BidLog;
import org.dromara.bidding.mapper.BidLogMapper;
import org.dromara.bidding.service.IBidLogService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 出价流水Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BidLogServiceImpl implements IBidLogService {

    private final BidLogMapper baseMapper;

    @Override
    public BidLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BidLogVo> queryPageList(BidLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BidLog> lqw = buildQueryWrapper(bo);
        Page<BidLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BidLogVo> queryList(BidLogBo bo) {
        LambdaQueryWrapper<BidLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BidLog> buildQueryWrapper(BidLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BidLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getBidLogCode()), BidLog::getBidLogCode, bo.getBidLogCode());
        lqw.eq(bo.getSessionId() != null, BidLog::getSessionId, bo.getSessionId());
        lqw.eq(bo.getItemId() != null, BidLog::getItemId, bo.getItemId());
        lqw.eq(bo.getUserId() != null, BidLog::getUserId, bo.getUserId());
        lqw.eq(bo.getBidStatus() != null, BidLog::getBidStatus, bo.getBidStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(BidLogBo bo) {
        BidLog add = MapstructUtils.convert(bo, BidLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(BidLogBo bo) {
        BidLog update = MapstructUtils.convert(bo, BidLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(BidLog entity){
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
