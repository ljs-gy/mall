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
import org.dromara.bidding.domain.bo.BidOrderBo;
import org.dromara.bidding.domain.vo.BidOrderVo;
import org.dromara.bidding.domain.BidOrder;
import org.dromara.bidding.mapper.BidOrderMapper;
import org.dromara.bidding.service.IBidOrderService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 竞价订单Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BidOrderServiceImpl implements IBidOrderService {

    private final BidOrderMapper baseMapper;

    @Override
    public BidOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BidOrderVo> queryPageList(BidOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BidOrder> lqw = buildQueryWrapper(bo);
        Page<BidOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BidOrderVo> queryList(BidOrderBo bo) {
        LambdaQueryWrapper<BidOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BidOrder> buildQueryWrapper(BidOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BidOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), BidOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getSessionId() != null, BidOrder::getSessionId, bo.getSessionId());
        lqw.eq(bo.getItemId() != null, BidOrder::getItemId, bo.getItemId());
        lqw.eq(bo.getUserId() != null, BidOrder::getUserId, bo.getUserId());
        lqw.eq(bo.getOrderStatus() != null, BidOrder::getOrderStatus, bo.getOrderStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(BidOrderBo bo) {
        BidOrder add = MapstructUtils.convert(bo, BidOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(BidOrderBo bo) {
        BidOrder update = MapstructUtils.convert(bo, BidOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(BidOrder entity){
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
