package org.dromara.goods.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.TreeBuildUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.goods.domain.CategoryBackend;
import org.dromara.goods.domain.bo.CategoryBackendBo;
import org.dromara.goods.domain.vo.CategoryBackendVo;
import org.dromara.goods.mapper.CategoryBackendMapper;
import org.dromara.goods.service.ICategoryBackendService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 后台商品分类Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryBackendServiceImpl implements ICategoryBackendService {

    private static final int MAX_LEVEL = 3;

    private final CategoryBackendMapper baseMapper;

    @Override
    public CategoryBackendVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<CategoryBackendVo> queryPageList(CategoryBackendBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CategoryBackend> lqw = buildQueryWrapper(bo);
        Page<CategoryBackendVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<CategoryBackendVo> queryList(CategoryBackendBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public List<Tree<Long>> selectTree() {
        List<CategoryBackendVo> list = baseMapper.selectVoList(Wrappers.lambdaQuery(CategoryBackend.class)
            .orderByAsc(CategoryBackend::getId));
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.buildMultiRoot(
            list,
            CategoryBackendVo::getId,
            CategoryBackendVo::getParentId,
            (node, treeNode) -> {
                treeNode.setId(node.getId());
                treeNode.setParentId(node.getParentId());
                treeNode.setName(node.getCategoryName());
                treeNode.setWeight(node.getId());
                treeNode.putExtra("typeId", node.getTypeId());
                treeNode.putExtra("logoUrl", node.getLogoUrl());
                treeNode.putExtra("remark", node.getRemark());
            }
        );
    }

    /**
     * 计算分类层级：根=1
     */
    @Override
    public int resolveLevel(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return 1;
        }
        int level = 1;
        Long current = parentId;
        while (current != null && current != 0L) {
            level++;
            if (level > MAX_LEVEL) {
                break;
            }
            CategoryBackend parent = baseMapper.selectById(current);
            if (parent == null) {
                throw new ServiceException("父分类不存在");
            }
            current = parent.getParentId();
        }
        return level;
    }

    private LambdaQueryWrapper<CategoryBackend> buildQueryWrapper(CategoryBackendBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CategoryBackend> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CategoryBackend::getId);
        lqw.like(StringUtils.isNotBlank(bo.getCategoryName()), CategoryBackend::getCategoryName, bo.getCategoryName());
        lqw.eq(bo.getParentId() != null, CategoryBackend::getParentId, bo.getParentId());
        lqw.eq(bo.getTypeId() != null, CategoryBackend::getTypeId, bo.getTypeId());
        lqw.eq(StringUtils.isNotBlank(bo.getLogoUrl()), CategoryBackend::getLogoUrl, bo.getLogoUrl());
        return lqw;
    }

    @Override
    public Boolean insertByBo(CategoryBackendBo bo) {
        CategoryBackend add = MapstructUtils.convert(bo, CategoryBackend.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(CategoryBackendBo bo) {
        CategoryBackend update = MapstructUtils.convert(bo, CategoryBackend.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(CategoryBackend entity) {
        if (entity.getParentId() == null) {
            entity.setParentId(0L);
        }
        int level = resolveLevel(entity.getParentId());
        if (level > MAX_LEVEL) {
            throw new ServiceException("分类最多支持三级");
        }
        if (level == MAX_LEVEL) {
            if (entity.getTypeId() == null) {
                throw new ServiceException("三级分类必须关联商品类型");
            }
        } else {
            if (entity.getTypeId() != null) {
                throw new ServiceException("仅三级分类可关联商品类型");
            }
            entity.setTypeId(null);
            entity.setLogoUrl(null);
        }
        // 修改时禁止把自己挂到子孙下（简单防环：不能 parentId = 自己）
        if (entity.getId() != null && entity.getId().equals(entity.getParentId())) {
            throw new ServiceException("父分类不能是自己");
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid)) {
            Long childCount = baseMapper.selectCount(Wrappers.lambdaQuery(CategoryBackend.class)
                .in(CategoryBackend::getParentId, ids));
            if (childCount != null && childCount > 0) {
                throw new ServiceException("存在子分类，不允许删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
