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
import org.dromara.bidding.domain.bo.AnnouncementItemGoodsBo;
import org.dromara.bidding.domain.vo.AnnouncementItemGoodsVo;
import org.dromara.bidding.domain.AnnouncementItemGoods;
import org.dromara.bidding.mapper.AnnouncementItemGoodsMapper;
import org.dromara.bidding.service.IAnnouncementItemGoodsService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 包裹子商品Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AnnouncementItemGoodsServiceImpl implements IAnnouncementItemGoodsService {

    private final AnnouncementItemGoodsMapper baseMapper;

    @Override
    public AnnouncementItemGoodsVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<AnnouncementItemGoodsVo> queryPageList(AnnouncementItemGoodsBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AnnouncementItemGoods> lqw = buildQueryWrapper(bo);
        Page<AnnouncementItemGoodsVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<AnnouncementItemGoodsVo> queryList(AnnouncementItemGoodsBo bo) {
        LambdaQueryWrapper<AnnouncementItemGoods> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AnnouncementItemGoods> buildQueryWrapper(AnnouncementItemGoodsBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AnnouncementItemGoods> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AnnouncementItemGoods::getId);
        lqw.like(StringUtils.isNotBlank(bo.getGoodsName()), AnnouncementItemGoods::getGoodsName, bo.getGoodsName());
        lqw.eq(bo.getItemId() != null, AnnouncementItemGoods::getItemId, bo.getItemId());
        lqw.eq(bo.getSubResourceId() != null, AnnouncementItemGoods::getSubResourceId, bo.getSubResourceId());
        return lqw;
    }

    @Override
    public Boolean insertByBo(AnnouncementItemGoodsBo bo) {
        AnnouncementItemGoods add = MapstructUtils.convert(bo, AnnouncementItemGoods.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(AnnouncementItemGoodsBo bo) {
        AnnouncementItemGoods update = MapstructUtils.convert(bo, AnnouncementItemGoods.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(AnnouncementItemGoods entity){
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
