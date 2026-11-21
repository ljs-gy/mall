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
import org.dromara.order.domain.bo.OrderTimeConfigBo;
import org.dromara.order.domain.vo.OrderTimeConfigVo;
import org.dromara.order.domain.OrderTimeConfig;
import org.dromara.order.mapper.OrderTimeConfigMapper;
import org.dromara.order.service.IOrderTimeConfigService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 订单交易时效配置Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderTimeConfigServiceImpl implements IOrderTimeConfigService {

    private final OrderTimeConfigMapper baseMapper;

    /**
     * 查询订单交易时效配置
     *
     * @param id 主键
     * @return 订单交易时效配置
     */
    @Override
    public OrderTimeConfigVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询订单交易时效配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单交易时效配置分页列表
     */
    @Override
    public TableDataInfo<OrderTimeConfigVo> queryPageList(OrderTimeConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OrderTimeConfig> lqw = buildQueryWrapper(bo);
        Page<OrderTimeConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的订单交易时效配置列表
     *
     * @param bo 查询条件
     * @return 订单交易时效配置列表
     */
    @Override
    public List<OrderTimeConfigVo> queryList(OrderTimeConfigBo bo) {
        LambdaQueryWrapper<OrderTimeConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<OrderTimeConfig> buildQueryWrapper(OrderTimeConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<OrderTimeConfig> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(OrderTimeConfig::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getChannelCode()), OrderTimeConfig::getChannelCode, bo.getChannelCode());
        lqw.eq(bo.getUnpaidCancelMinutes() != null, OrderTimeConfig::getUnpaidCancelMinutes, bo.getUnpaidCancelMinutes());
        lqw.eq(bo.getAutoReceiveDays() != null, OrderTimeConfig::getAutoReceiveDays, bo.getAutoReceiveDays());
        lqw.eq(bo.getAutoCompleteDays() != null, OrderTimeConfig::getAutoCompleteDays, bo.getAutoCompleteDays());
        lqw.eq(bo.getStatus() != null, OrderTimeConfig::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增订单交易时效配置
     *
     * @param bo 订单交易时效配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(OrderTimeConfigBo bo) {
        OrderTimeConfig add = MapstructUtils.convert(bo, OrderTimeConfig.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改订单交易时效配置
     *
     * @param bo 订单交易时效配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(OrderTimeConfigBo bo) {
        OrderTimeConfig update = MapstructUtils.convert(bo, OrderTimeConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(OrderTimeConfig entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除订单交易时效配置信息
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
