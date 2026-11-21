package org.dromara.pm.service.impl;

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
import org.dromara.pm.domain.bo.CouponUserRelBo;
import org.dromara.pm.domain.vo.CouponUserRelVo;
import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.service.ICouponUserRelService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户优惠券领用记录Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CouponUserRelServiceImpl implements ICouponUserRelService {

    private final CouponUserRelMapper baseMapper;

    /**
     * 查询用户优惠券领用记录
     *
     * @param id 主键
     * @return 用户优惠券领用记录
     */
    @Override
    public CouponUserRelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户优惠券领用记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户优惠券领用记录分页列表
     */
    @Override
    public TableDataInfo<CouponUserRelVo> queryPageList(CouponUserRelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CouponUserRel> lqw = buildQueryWrapper(bo);
        Page<CouponUserRelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户优惠券领用记录列表
     *
     * @param bo 查询条件
     * @return 用户优惠券领用记录列表
     */
    @Override
    public List<CouponUserRelVo> queryList(CouponUserRelBo bo) {
        LambdaQueryWrapper<CouponUserRel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CouponUserRel> buildQueryWrapper(CouponUserRelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CouponUserRel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CouponUserRel::getId);
        lqw.eq(bo.getUserId() != null, CouponUserRel::getUserId, bo.getUserId());
        lqw.eq(bo.getCouponTemplateId() != null, CouponUserRel::getCouponTemplateId, bo.getCouponTemplateId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserCouponCode()), CouponUserRel::getUserCouponCode, bo.getUserCouponCode());
        lqw.eq(bo.getMinSpendAmount() != null, CouponUserRel::getMinSpendAmount, bo.getMinSpendAmount());
        lqw.eq(bo.getBenefitValue() != null, CouponUserRel::getBenefitValue, bo.getBenefitValue());
        lqw.eq(bo.getStartTime() != null, CouponUserRel::getStartTime, bo.getStartTime());
        lqw.eq(bo.getEndTime() != null, CouponUserRel::getEndTime, bo.getEndTime());
        lqw.eq(bo.getStatus() != null, CouponUserRel::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增用户优惠券领用记录
     *
     * @param bo 用户优惠券领用记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CouponUserRelBo bo) {
        CouponUserRel add = MapstructUtils.convert(bo, CouponUserRel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户优惠券领用记录
     *
     * @param bo 用户优惠券领用记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CouponUserRelBo bo) {
        CouponUserRel update = MapstructUtils.convert(bo, CouponUserRel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CouponUserRel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户优惠券领用记录信息
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
