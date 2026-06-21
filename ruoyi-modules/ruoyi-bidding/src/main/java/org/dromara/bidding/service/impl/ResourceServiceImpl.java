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
import org.dromara.bidding.domain.bo.ResourceBo;
import org.dromara.bidding.domain.vo.ResourceVo;
import org.dromara.bidding.domain.Resource;
import org.dromara.bidding.mapper.ResourceMapper;
import org.dromara.bidding.service.IResourceService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 竞价资源主Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ResourceServiceImpl implements IResourceService {

    private final ResourceMapper baseMapper;

    /**
     * 查询竞价资源主
     *
     * @param id 主键
     * @return 竞价资源主
     */
    @Override
    public ResourceVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询竞价资源主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 竞价资源主分页列表
     */
    @Override
    public TableDataInfo<ResourceVo> queryPageList(ResourceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Resource> lqw = buildQueryWrapper(bo);
        Page<ResourceVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的竞价资源主列表
     *
     * @param bo 查询条件
     * @return 竞价资源主列表
     */
    @Override
    public List<ResourceVo> queryList(ResourceBo bo) {
        LambdaQueryWrapper<Resource> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Resource> buildQueryWrapper(ResourceBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Resource> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Resource::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getResourceCode()), Resource::getResourceCode, bo.getResourceCode());
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), Resource::getTitle, bo.getTitle());
        lqw.eq(bo.getCategoryId() != null, Resource::getCategoryId, bo.getCategoryId());
        lqw.eq(StringUtils.isNotBlank(bo.getBrandSpec()), Resource::getBrandSpec, bo.getBrandSpec());
        lqw.eq(bo.getWarehouseId() != null, Resource::getWarehouseId, bo.getWarehouseId());
        lqw.eq(StringUtils.isNotBlank(bo.getSummary()), Resource::getSummary, bo.getSummary());
        lqw.eq(StringUtils.isNotBlank(bo.getAddress()), Resource::getAddress, bo.getAddress());
        lqw.eq(bo.getFloorPrice() != null, Resource::getFloorPrice, bo.getFloorPrice());
        lqw.eq(bo.getQuantity() != null, Resource::getQuantity, bo.getQuantity());
        lqw.eq(StringUtils.isNotBlank(bo.getUnit()), Resource::getUnit, bo.getUnit());
        lqw.eq(StringUtils.isNotBlank(bo.getImages()), Resource::getImages, bo.getImages());
        lqw.eq(StringUtils.isNotBlank(bo.getVideoUrl()), Resource::getVideoUrl, bo.getVideoUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Resource::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增竞价资源主
     *
     * @param bo 竞价资源主
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ResourceBo bo) {
        Resource add = MapstructUtils.convert(bo, Resource.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改竞价资源主
     *
     * @param bo 竞价资源主
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ResourceBo bo) {
        Resource update = MapstructUtils.convert(bo, Resource.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Resource entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除竞价资源主信息
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
