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
import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.bidding.domain.bo.BidOrderBo;
import org.dromara.bidding.domain.vo.BidOrderVo;
import org.dromara.bidding.domain.BidOrder;
import org.dromara.bidding.mapper.AnnouncementItemMapper;
import org.dromara.bidding.mapper.BidOrderMapper;
import org.dromara.bidding.service.IBidOrderService;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.redis.utils.SequenceUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final AnnouncementItemMapper announcementItemMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateStrikeOrder(Long itemId) {
        AnnouncementItem item = announcementItemMapper.selectById(itemId);
        if (item == null) {
            log.warn("标的 {} 不存在，跳过生单", itemId);
            return;
        }
        if (item.getCurrentMaxUserId() == null
            || item.getTotalBidCount() == null
            || item.getTotalBidCount() <= 0) {
            log.info("标的 {} 无人出价，不生单", itemId);
            return;
        }
        Long exists = baseMapper.selectCount(Wrappers.<BidOrder>lambdaQuery()
            .eq(BidOrder::getItemId, itemId));
        if (exists != null && exists > 0) {
            log.info("标的 {} 已存在订单，跳过重复生单", itemId);
            return;
        }

        BidOrder order = new BidOrder();
        order.setOrderNo(SequenceUtils.getDateTimeId("BO", true));
        order.setSessionId(item.getSessionId());
        order.setItemId(itemId);
        order.setUserId(item.getCurrentMaxUserId());
        order.setStrikePrice(defaultZero(item.getAuctionGinfoPrice()));
        order.setQuantity(defaultZero(item.getGoodsQuantity()));
        order.setTotalGoodsAmount(defaultZero(item.getAuctionGinfoMoney()));
        order.setDepositOffsetAmount(BigDecimal.ZERO);
        order.setCommissionAmount(BigDecimal.ZERO);
        order.setFinalPendingAmount(defaultZero(item.getAuctionGinfoMoney()));
        order.setOrderStatus(0);
        baseMapper.insert(order);
        log.info("标的 {} 落槌生单成功，订单号 {}", itemId, order.getOrderNo());
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
