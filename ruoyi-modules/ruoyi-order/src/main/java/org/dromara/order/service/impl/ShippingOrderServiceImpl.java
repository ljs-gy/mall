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
import org.dromara.order.domain.bo.ShippingOrderBo;
import org.dromara.order.domain.vo.ShippingOrderVo;
import org.dromara.order.domain.ShippingOrder;
import org.dromara.order.mapper.ShippingOrderMapper;
import org.dromara.order.service.IShippingOrderService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 发货单Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ShippingOrderServiceImpl implements IShippingOrderService {

    private final ShippingOrderMapper baseMapper;

    /**
     * 查询发货单
     *
     * @param id 主键
     * @return 发货单
     */
    @Override
    public ShippingOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询发货单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 发货单分页列表
     */
    @Override
    public TableDataInfo<ShippingOrderVo> queryPageList(ShippingOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ShippingOrder> lqw = buildQueryWrapper(bo);
        Page<ShippingOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的发货单列表
     *
     * @param bo 查询条件
     * @return 发货单列表
     */
    @Override
    public List<ShippingOrderVo> queryList(ShippingOrderBo bo) {
        LambdaQueryWrapper<ShippingOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ShippingOrder> buildQueryWrapper(ShippingOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ShippingOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ShippingOrder::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getShippingNo()), ShippingOrder::getShippingNo, bo.getShippingNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), ShippingOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getUserinfoId() != null, ShippingOrder::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberBcode()), ShippingOrder::getMemberBcode, bo.getMemberBcode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), ShippingOrder::getMemberCode, bo.getMemberCode());
        lqw.eq(bo.getShippingStatus() != null, ShippingOrder::getShippingStatus, bo.getShippingStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getExpressCompany()), ShippingOrder::getExpressCompany, bo.getExpressCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getExpressNo()), ShippingOrder::getExpressNo, bo.getExpressNo());
        lqw.like(StringUtils.isNotBlank(bo.getConsigneeName()), ShippingOrder::getConsigneeName, bo.getConsigneeName());
        lqw.eq(StringUtils.isNotBlank(bo.getConsigneeMobile()), ShippingOrder::getConsigneeMobile, bo.getConsigneeMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getReceiverAddressText()), ShippingOrder::getReceiverAddressText, bo.getReceiverAddressText());
        lqw.eq(bo.getShippingTime() != null, ShippingOrder::getShippingTime, bo.getShippingTime());
        lqw.eq(bo.getReceiveTime() != null, ShippingOrder::getReceiveTime, bo.getReceiveTime());
        return lqw;
    }

    /**
     * 新增发货单
     *
     * @param bo 发货单
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ShippingOrderBo bo) {
        ShippingOrder add = MapstructUtils.convert(bo, ShippingOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改发货单
     *
     * @param bo 发货单
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ShippingOrderBo bo) {
        ShippingOrder update = MapstructUtils.convert(bo, ShippingOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ShippingOrder entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除发货单信息
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
