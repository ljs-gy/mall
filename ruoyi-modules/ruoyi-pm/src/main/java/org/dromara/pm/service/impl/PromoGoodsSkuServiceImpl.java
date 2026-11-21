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
import org.dromara.pm.domain.bo.PromoGoodsSkuBo;
import org.dromara.pm.domain.vo.PromoGoodsSkuVo;
import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.mapper.PromoGoodsSkuMapper;
import org.dromara.pm.service.IPromoGoodsSkuService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 活动商品配置Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PromoGoodsSkuServiceImpl implements IPromoGoodsSkuService {

    private final PromoGoodsSkuMapper baseMapper;

    /**
     * 查询活动商品配置
     *
     * @param id 主键
     * @return 活动商品配置
     */
    @Override
    public PromoGoodsSkuVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询活动商品配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 活动商品配置分页列表
     */
    @Override
    public TableDataInfo<PromoGoodsSkuVo> queryPageList(PromoGoodsSkuBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PromoGoodsSku> lqw = buildQueryWrapper(bo);
        Page<PromoGoodsSkuVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的活动商品配置列表
     *
     * @param bo 查询条件
     * @return 活动商品配置列表
     */
    @Override
    public List<PromoGoodsSkuVo> queryList(PromoGoodsSkuBo bo) {
        LambdaQueryWrapper<PromoGoodsSku> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PromoGoodsSku> buildQueryWrapper(PromoGoodsSkuBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<PromoGoodsSku> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(PromoGoodsSku::getId);
        lqw.eq(bo.getActivityId() != null, PromoGoodsSku::getActivityId, bo.getActivityId());
        lqw.eq(bo.getSpuId() != null, PromoGoodsSku::getSpuId, bo.getSpuId());
        lqw.eq(bo.getSkuId() != null, PromoGoodsSku::getSkuId, bo.getSkuId());
        lqw.eq(bo.getPromoPrice() != null, PromoGoodsSku::getPromoPrice, bo.getPromoPrice());
        lqw.eq(bo.getActivityStock() != null, PromoGoodsSku::getActivityStock, bo.getActivityStock());
        lqw.eq(bo.getSoldStock() != null, PromoGoodsSku::getSoldStock, bo.getSoldStock());
        lqw.eq(bo.getUserLimitQty() != null, PromoGoodsSku::getUserLimitQty, bo.getUserLimitQty());
        lqw.eq(bo.getVersion() != null, PromoGoodsSku::getVersion, bo.getVersion());
        return lqw;
    }

    /**
     * 新增活动商品配置
     *
     * @param bo 活动商品配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PromoGoodsSkuBo bo) {
        PromoGoodsSku add = MapstructUtils.convert(bo, PromoGoodsSku.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改活动商品配置
     *
     * @param bo 活动商品配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PromoGoodsSkuBo bo) {
        PromoGoodsSku update = MapstructUtils.convert(bo, PromoGoodsSku.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(PromoGoodsSku entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除活动商品配置信息
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
