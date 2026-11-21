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
import org.dromara.order.domain.bo.AfterSaleItemBo;
import org.dromara.order.domain.vo.AfterSaleItemVo;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.mapper.AfterSaleItemMapper;
import org.dromara.order.service.IAfterSaleItemService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 售后单明细Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AfterSaleItemServiceImpl implements IAfterSaleItemService {

    private final AfterSaleItemMapper baseMapper;

    /**
     * 查询售后单明细
     *
     * @param id 主键
     * @return 售后单明细
     */
    @Override
    public AfterSaleItemVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询售后单明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 售后单明细分页列表
     */
    @Override
    public TableDataInfo<AfterSaleItemVo> queryPageList(AfterSaleItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AfterSaleItem> lqw = buildQueryWrapper(bo);
        Page<AfterSaleItemVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的售后单明细列表
     *
     * @param bo 查询条件
     * @return 售后单明细列表
     */
    @Override
    public List<AfterSaleItemVo> queryList(AfterSaleItemBo bo) {
        LambdaQueryWrapper<AfterSaleItem> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AfterSaleItem> buildQueryWrapper(AfterSaleItemBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AfterSaleItem> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AfterSaleItem::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getAfterSaleNo()), AfterSaleItem::getAfterSaleNo, bo.getAfterSaleNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), AfterSaleItem::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getOrderItemId() != null, AfterSaleItem::getOrderItemId, bo.getOrderItemId());
        lqw.eq(bo.getSkuId() != null, AfterSaleItem::getSkuId, bo.getSkuId());
        lqw.eq(bo.getReturnQuantity() != null, AfterSaleItem::getReturnQuantity, bo.getReturnQuantity());
        lqw.eq(bo.getRefundAmount() != null, AfterSaleItem::getRefundAmount, bo.getRefundAmount());
        return lqw;
    }

    /**
     * 新增售后单明细
     *
     * @param bo 售后单明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AfterSaleItemBo bo) {
        AfterSaleItem add = MapstructUtils.convert(bo, AfterSaleItem.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改售后单明细
     *
     * @param bo 售后单明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AfterSaleItemBo bo) {
        AfterSaleItem update = MapstructUtils.convert(bo, AfterSaleItem.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AfterSaleItem entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除售后单明细信息
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
