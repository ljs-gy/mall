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
import org.dromara.order.domain.bo.OrderItemBo;
import org.dromara.order.domain.vo.OrderItemVo;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.service.IOrderItemService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 订单明细Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderItemServiceImpl implements IOrderItemService {

    private final OrderItemMapper baseMapper;

    /**
     * 查询订单明细
     *
     * @param id 主键
     * @return 订单明细
     */
    @Override
    public OrderItemVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询订单明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单明细分页列表
     */
    @Override
    public TableDataInfo<OrderItemVo> queryPageList(OrderItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OrderItem> lqw = buildQueryWrapper(bo);
        Page<OrderItemVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的订单明细列表
     *
     * @param bo 查询条件
     * @return 订单明细列表
     */
    @Override
    public List<OrderItemVo> queryList(OrderItemBo bo) {
        LambdaQueryWrapper<OrderItem> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<OrderItem> buildQueryWrapper(OrderItemBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<OrderItem> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(OrderItem::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), OrderItem::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getSpuId() != null, OrderItem::getSpuId, bo.getSpuId());
        lqw.eq(bo.getSkuId() != null, OrderItem::getSkuId, bo.getSkuId());
        lqw.eq(StringUtils.isNotBlank(bo.getSkuCode()), OrderItem::getSkuCode, bo.getSkuCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), OrderItem::getMemberCode, bo.getMemberCode());
        lqw.eq(StringUtils.isNotBlank(bo.getGoodsTitle()), OrderItem::getGoodsTitle, bo.getGoodsTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getSpecText()), OrderItem::getSpecText, bo.getSpecText());
        lqw.eq(StringUtils.isNotBlank(bo.getCoverImage()), OrderItem::getCoverImage, bo.getCoverImage());
        lqw.eq(bo.getPriceSales() != null, OrderItem::getPriceSales, bo.getPriceSales());
        lqw.eq(bo.getQuantity() != null, OrderItem::getQuantity, bo.getQuantity());
        lqw.eq(bo.getIsGift() != null, OrderItem::getIsGift, bo.getIsGift());
        lqw.eq(bo.getTierRuleId() != null, OrderItem::getTierRuleId, bo.getTierRuleId());
        lqw.eq(bo.getSubTotal() != null, OrderItem::getSubTotal, bo.getSubTotal());
        lqw.eq(bo.getRealPayAmount() != null, OrderItem::getRealPayAmount, bo.getRealPayAmount());
        return lqw;
    }

    /**
     * 新增订单明细
     *
     * @param bo 订单明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(OrderItemBo bo) {
        OrderItem add = MapstructUtils.convert(bo, OrderItem.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改订单明细
     *
     * @param bo 订单明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(OrderItemBo bo) {
        OrderItem update = MapstructUtils.convert(bo, OrderItem.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(OrderItem entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除订单明细信息
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
