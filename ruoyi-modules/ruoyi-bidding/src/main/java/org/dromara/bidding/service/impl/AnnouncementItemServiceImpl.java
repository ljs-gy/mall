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
import org.dromara.bidding.domain.bo.AnnouncementItemBo;
import org.dromara.bidding.domain.vo.AnnouncementItemVo;
import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.bidding.mapper.AnnouncementItemMapper;
import org.dromara.bidding.service.IAnnouncementItemService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 竞价标的Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AnnouncementItemServiceImpl implements IAnnouncementItemService {

    private final AnnouncementItemMapper baseMapper;

    @Override
    public AnnouncementItemVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<AnnouncementItemVo> queryPageList(AnnouncementItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AnnouncementItem> lqw = buildQueryWrapper(bo);
        Page<AnnouncementItemVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<AnnouncementItemVo> queryList(AnnouncementItemBo bo) {
        LambdaQueryWrapper<AnnouncementItem> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AnnouncementItem> buildQueryWrapper(AnnouncementItemBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AnnouncementItem> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getItemCode()), AnnouncementItem::getItemCode, bo.getItemCode());
        lqw.like(StringUtils.isNotBlank(bo.getItemName()), AnnouncementItem::getItemName, bo.getItemName());
        lqw.eq(bo.getSessionId() != null, AnnouncementItem::getSessionId, bo.getSessionId());
        lqw.eq(bo.getItemType() != null, AnnouncementItem::getItemType, bo.getItemType());
        lqw.eq(bo.getResourceId() != null, AnnouncementItem::getResourceId, bo.getResourceId());
        lqw.eq(bo.getItemStatus() != null, AnnouncementItem::getItemStatus, bo.getItemStatus());
        lqw.eq(bo.getCurrentMaxUserId() != null, AnnouncementItem::getCurrentMaxUserId, bo.getCurrentMaxUserId());
        return lqw;
    }

    @Override
    public Boolean insertByBo(AnnouncementItemBo bo) {
        AnnouncementItem add = MapstructUtils.convert(bo, AnnouncementItem.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(AnnouncementItemBo bo) {
        AnnouncementItem update = MapstructUtils.convert(bo, AnnouncementItem.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(AnnouncementItem entity){
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
