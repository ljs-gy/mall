package org.dromara.user.service.impl;

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
import org.dromara.user.domain.bo.StoreBo;
import org.dromara.user.domain.vo.StoreVo;
import org.dromara.user.domain.Store;
import org.dromara.user.mapper.StoreMapper;
import org.dromara.user.service.IStoreService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商家店铺Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StoreServiceImpl implements IStoreService {

    private final StoreMapper baseMapper;

    /**
     * 查询商家店铺
     *
     * @param id 主键
     * @return 商家店铺
     */
    @Override
    public StoreVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商家店铺列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家店铺分页列表
     */
    @Override
    public TableDataInfo<StoreVo> queryPageList(StoreBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Store> lqw = buildQueryWrapper(bo);
        Page<StoreVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商家店铺列表
     *
     * @param bo 查询条件
     * @return 商家店铺列表
     */
    @Override
    public List<StoreVo> queryList(StoreBo bo) {
        LambdaQueryWrapper<Store> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Store> buildQueryWrapper(StoreBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Store> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Store::getId);
        lqw.eq(bo.getUserinfoId() != null, Store::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getStoreNo()), Store::getStoreNo, bo.getStoreNo());
        lqw.like(StringUtils.isNotBlank(bo.getStoreName()), Store::getStoreName, bo.getStoreName());
        lqw.eq(StringUtils.isNotBlank(bo.getStoreLogoUrl()), Store::getStoreLogoUrl, bo.getStoreLogoUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getStoreIntro()), Store::getStoreIntro, bo.getStoreIntro());
        lqw.eq(bo.getStatus() != null, Store::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增商家店铺
     *
     * @param bo 商家店铺
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(StoreBo bo) {
        Store add = MapstructUtils.convert(bo, Store.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商家店铺
     *
     * @param bo 商家店铺
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(StoreBo bo) {
        Store update = MapstructUtils.convert(bo, Store.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Store entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商家店铺信息
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
