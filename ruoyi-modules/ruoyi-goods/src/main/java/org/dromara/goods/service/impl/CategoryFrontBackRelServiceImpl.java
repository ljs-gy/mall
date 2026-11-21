package org.dromara.goods.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.goods.domain.bo.CategoryFrontBackRelBo;
import org.dromara.goods.domain.vo.CategoryFrontBackRelVo;
import org.dromara.goods.domain.CategoryFrontBackRel;
import org.dromara.goods.mapper.CategoryFrontBackRelMapper;
import org.dromara.goods.service.ICategoryFrontBackRelService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 前后台分类映射关系Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryFrontBackRelServiceImpl implements ICategoryFrontBackRelService {

    private final CategoryFrontBackRelMapper baseMapper;

    @Override
    public CategoryFrontBackRelVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<CategoryFrontBackRelVo> queryPageList(CategoryFrontBackRelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CategoryFrontBackRel> lqw = buildQueryWrapper(bo);
        Page<CategoryFrontBackRelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<CategoryFrontBackRelVo> queryList(CategoryFrontBackRelBo bo) {
        LambdaQueryWrapper<CategoryFrontBackRel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CategoryFrontBackRel> buildQueryWrapper(CategoryFrontBackRelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CategoryFrontBackRel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CategoryFrontBackRel::getId);
        lqw.eq(bo.getFrontendCategoryId() != null, CategoryFrontBackRel::getFrontendCategoryId, bo.getFrontendCategoryId());
        lqw.eq(bo.getBackendCategoryId() != null, CategoryFrontBackRel::getBackendCategoryId, bo.getBackendCategoryId());
        return lqw;
    }

    @Override
    public Boolean insertByBo(CategoryFrontBackRelBo bo) {
        CategoryFrontBackRel add = MapstructUtils.convert(bo, CategoryFrontBackRel.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(CategoryFrontBackRelBo bo) {
        CategoryFrontBackRel update = MapstructUtils.convert(bo, CategoryFrontBackRel.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }
}
