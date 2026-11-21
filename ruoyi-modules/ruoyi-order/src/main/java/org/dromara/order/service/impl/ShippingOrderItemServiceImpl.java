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
import org.dromara.order.domain.bo.ShippingOrderItemBo;
import org.dromara.order.domain.vo.ShippingOrderItemVo;
import org.dromara.order.domain.ShippingOrderItem;
import org.dromara.order.mapper.ShippingOrderItemMapper;
import org.dromara.order.service.IShippingOrderItemService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 发货单包裹明细Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ShippingOrderItemServiceImpl implements IShippingOrderItemService {

    private final ShippingOrderItemMapper baseMapper;

    /**
     * 查询发货单包裹明细
     *
     * @param id 主键
     * @return 发货单包裹明细
     */
    @Override
    public ShippingOrderItemVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询发货单包裹明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 发货单包裹明细分页列表
     */
    @Override
    public TableDataInfo<ShippingOrderItemVo> queryPageList(ShippingOrderItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ShippingOrderItem> lqw = buildQueryWrapper(bo);
        Page<ShippingOrderItemVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的发货单包裹明细列表
     *
     * @param bo 查询条件
     * @return 发货单包裹明细列表
     */
    @Override
    public List<ShippingOrderItemVo> queryList(ShippingOrderItemBo bo) {
        LambdaQueryWrapper<ShippingOrderItem> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ShippingOrderItem> buildQueryWrapper(ShippingOrderItemBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ShippingOrderItem> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ShippingOrderItem::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getShippingNo()), ShippingOrderItem::getShippingNo, bo.getShippingNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), ShippingOrderItem::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getOrderItemId() != null, ShippingOrderItem::getOrderItemId, bo.getOrderItemId());
        lqw.eq(bo.getSkuId() != null, ShippingOrderItem::getSkuId, bo.getSkuId());
        lqw.eq(bo.getShippingQuantity() != null, ShippingOrderItem::getShippingQuantity, bo.getShippingQuantity());
        return lqw;
    }

    /**
     * 新增发货单包裹明细
     *
     * @param bo 发货单包裹明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ShippingOrderItemBo bo) {
        ShippingOrderItem add = MapstructUtils.convert(bo, ShippingOrderItem.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改发货单包裹明细
     *
     * @param bo 发货单包裹明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ShippingOrderItemBo bo) {
        ShippingOrderItem update = MapstructUtils.convert(bo, ShippingOrderItem.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ShippingOrderItem entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除发货单包裹明细信息
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
