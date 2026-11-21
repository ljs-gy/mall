package org.dromara.order.service.impl;

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
import org.dromara.order.domain.bo.OrderBo;
import org.dromara.order.domain.vo.OrderVo;
import org.dromara.order.domain.Order;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 订单主Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private final OrderMapper baseMapper;

    /**
     * 查询订单主
     *
     * @param id 主键
     * @return 订单主
     */
    @Override
    public OrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询订单主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单主分页列表
     */
    @Override
    public TableDataInfo<OrderVo> queryPageList(OrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Order> lqw = buildQueryWrapper(bo);
        Page<OrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的订单主列表
     *
     * @param bo 查询条件
     * @return 订单主列表
     */
    @Override
    public List<OrderVo> queryList(OrderBo bo) {
        LambdaQueryWrapper<Order> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Order> buildQueryWrapper(OrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Order> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Order::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), Order::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getParentOrderNo()), Order::getParentOrderNo, bo.getParentOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getPayTradeNo()), Order::getPayTradeNo, bo.getPayTradeNo());
        lqw.eq(StringUtils.isNotBlank(bo.getChannelCode()), Order::getChannelCode, bo.getChannelCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), Order::getMemberCode, bo.getMemberCode());
        lqw.eq(bo.getUserinfoId() != null, Order::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberBcode()), Order::getMemberBcode, bo.getMemberBcode());
        lqw.eq(bo.getOrderStatus() != null, Order::getOrderStatus, bo.getOrderStatus());
        lqw.eq(bo.getPayStatus() != null, Order::getPayStatus, bo.getPayStatus());
        lqw.eq(bo.getGoodsTotalAmount() != null, Order::getGoodsTotalAmount, bo.getGoodsTotalAmount());
        lqw.eq(bo.getFreightAmount() != null, Order::getFreightAmount, bo.getFreightAmount());
        lqw.eq(bo.getDiscountAmount() != null, Order::getDiscountAmount, bo.getDiscountAmount());
        lqw.eq(bo.getPayAmount() != null, Order::getPayAmount, bo.getPayAmount());
        lqw.eq(bo.getPayTime() != null, Order::getPayTime, bo.getPayTime());
        lqw.eq(bo.getShippingTime() != null, Order::getShippingTime, bo.getShippingTime());
        lqw.eq(bo.getReceiveTime() != null, Order::getReceiveTime, bo.getReceiveTime());
        lqw.like(StringUtils.isNotBlank(bo.getConsigneeName()), Order::getConsigneeName, bo.getConsigneeName());
        lqw.eq(StringUtils.isNotBlank(bo.getConsigneeMobile()), Order::getConsigneeMobile, bo.getConsigneeMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getReceiverAddressText()), Order::getReceiverAddressText, bo.getReceiverAddressText());
        lqw.eq(StringUtils.isNotBlank(bo.getBuyerRemark()), Order::getBuyerRemark, bo.getBuyerRemark());
        return lqw;
    }

    /**
     * 新增订单主
     *
     * @param bo 订单主
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(OrderBo bo) {
        Order add = MapstructUtils.convert(bo, Order.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改订单主
     *
     * @param bo 订单主
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(OrderBo bo) {
        Order update = MapstructUtils.convert(bo, Order.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Order entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除订单主信息
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
