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
import org.dromara.order.domain.bo.AfterSaleBo;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.service.IAfterSaleService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 售后退货单Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AfterSaleServiceImpl implements IAfterSaleService {

    private final AfterSaleMapper baseMapper;

    /**
     * 查询售后退货单
     *
     * @param id 主键
     * @return 售后退货单
     */
    @Override
    public AfterSaleVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询售后退货单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 售后退货单分页列表
     */
    @Override
    public TableDataInfo<AfterSaleVo> queryPageList(AfterSaleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AfterSale> lqw = buildQueryWrapper(bo);
        Page<AfterSaleVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的售后退货单列表
     *
     * @param bo 查询条件
     * @return 售后退货单列表
     */
    @Override
    public List<AfterSaleVo> queryList(AfterSaleBo bo) {
        LambdaQueryWrapper<AfterSale> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AfterSale> buildQueryWrapper(AfterSaleBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AfterSale> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AfterSale::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getAfterSaleNo()), AfterSale::getAfterSaleNo, bo.getAfterSaleNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), AfterSale::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getUserinfoId() != null, AfterSale::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberBcode()), AfterSale::getMemberBcode, bo.getMemberBcode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), AfterSale::getMemberCode, bo.getMemberCode());
        lqw.eq(bo.getServiceType() != null, AfterSale::getServiceType, bo.getServiceType());
        lqw.eq(bo.getApplySource() != null, AfterSale::getApplySource, bo.getApplySource());
        lqw.eq(bo.getStatus() != null, AfterSale::getStatus, bo.getStatus());
        lqw.eq(bo.getRefundAmount() != null, AfterSale::getRefundAmount, bo.getRefundAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyReason()), AfterSale::getApplyReason, bo.getApplyReason());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyRemark()), AfterSale::getApplyRemark, bo.getApplyRemark());
        lqw.eq(StringUtils.isNotBlank(bo.getReturnExpressCompany()), AfterSale::getReturnExpressCompany, bo.getReturnExpressCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getReturnExpressNo()), AfterSale::getReturnExpressNo, bo.getReturnExpressNo());
        lqw.eq(bo.getReturnTime() != null, AfterSale::getReturnTime, bo.getReturnTime());
        lqw.eq(bo.getAuditTime() != null, AfterSale::getAuditTime, bo.getAuditTime());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditBy()), AfterSale::getAuditBy, bo.getAuditBy());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditRemark()), AfterSale::getAuditRemark, bo.getAuditRemark());
        lqw.eq(bo.getInspectTime() != null, AfterSale::getInspectTime, bo.getInspectTime());
        lqw.eq(bo.getRefundTime() != null, AfterSale::getRefundTime, bo.getRefundTime());
        return lqw;
    }

    /**
     * 新增售后退货单
     *
     * @param bo 售后退货单
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AfterSaleBo bo) {
        AfterSale add = MapstructUtils.convert(bo, AfterSale.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改售后退货单
     *
     * @param bo 售后退货单
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AfterSaleBo bo) {
        AfterSale update = MapstructUtils.convert(bo, AfterSale.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AfterSale entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除售后退货单信息
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
