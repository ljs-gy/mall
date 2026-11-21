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
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.goods.domain.CategoryBackend;
import org.dromara.goods.domain.CategoryFrontBackRel;
import org.dromara.goods.domain.CategoryFrontend;
import org.dromara.goods.domain.bo.CategoryFrontendBo;
import org.dromara.goods.domain.vo.CategoryBackendVo;
import org.dromara.goods.domain.vo.CategoryFrontendVo;
import org.dromara.goods.mapper.CategoryBackendMapper;
import org.dromara.goods.mapper.CategoryFrontBackRelMapper;
import org.dromara.goods.mapper.CategoryFrontendMapper;
import org.dromara.goods.service.ICategoryBackendService;
import org.dromara.goods.service.ICategoryFrontendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 前台商品分类Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryFrontendServiceImpl implements ICategoryFrontendService {

    private static final int MAX_LEVEL = 3;

    private final CategoryFrontendMapper baseMapper;
    private final CategoryFrontBackRelMapper frontBackRelMapper;
    private final CategoryBackendMapper categoryBackendMapper;
    private final ICategoryBackendService categoryBackendService;

    @Override
    public CategoryFrontendVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<CategoryFrontendVo> queryPageList(CategoryFrontendBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CategoryFrontend> lqw = buildQueryWrapper(bo);
        Page<CategoryFrontendVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<CategoryFrontendVo> queryList(CategoryFrontendBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public List<Tree<Long>> selectTree() {
        List<CategoryFrontendVo> list = baseMapper.selectVoList(Wrappers.lambdaQuery(CategoryFrontend.class)
            .orderByAsc(CategoryFrontend::getId));
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.buildMultiRoot(
            list,
            CategoryFrontendVo::getId,
            CategoryFrontendVo::getParentId,
            (node, treeNode) -> {
                treeNode.setId(node.getId());
                treeNode.setParentId(node.getParentId());
                treeNode.setName(node.getFrontCategoryName());
                treeNode.setWeight(node.getId());
                treeNode.putExtra("logoUrl", node.getLogoUrl());
                treeNode.putExtra("isHidden", node.getIsHidden());
            }
        );
    }

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
            CategoryFrontend parent = baseMapper.selectById(current);
            if (parent == null) {
                throw new ServiceException("父分类不存在");
            }
            current = parent.getParentId();
        }
        return level;
    }

    @Override
    public List<CategoryBackendVo> listBackendOptions(Long frontendCategoryId) {
        CategoryFrontend frontend = baseMapper.selectById(frontendCategoryId);
        if (frontend == null) {
            throw new ServiceException("前台分类不存在");
        }
        if (resolveLevel(frontend.getParentId()) != MAX_LEVEL) {
            throw new ServiceException("仅三级前台分类可查询关联后台分类");
        }
        List<CategoryFrontBackRel> rels = frontBackRelMapper.selectList(Wrappers.lambdaQuery(CategoryFrontBackRel.class)
            .eq(CategoryFrontBackRel::getFrontendCategoryId, frontendCategoryId));
        if (CollUtil.isEmpty(rels)) {
            return CollUtil.newArrayList();
        }
        List<Long> backendIds = rels.stream().map(CategoryFrontBackRel::getBackendCategoryId).filter(Objects::nonNull).toList();
        if (CollUtil.isEmpty(backendIds)) {
            return CollUtil.newArrayList();
        }
        return categoryBackendMapper.selectVoList(Wrappers.lambdaQuery(CategoryBackend.class)
            .in(CategoryBackend::getId, backendIds)
            .orderByAsc(CategoryBackend::getId));
    }

    private LambdaQueryWrapper<CategoryFrontend> buildQueryWrapper(CategoryFrontendBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CategoryFrontend> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CategoryFrontend::getId);
        lqw.like(StringUtils.isNotBlank(bo.getFrontCategoryName()), CategoryFrontend::getFrontCategoryName, bo.getFrontCategoryName());
        lqw.eq(bo.getParentId() != null, CategoryFrontend::getParentId, bo.getParentId());
        lqw.eq(StringUtils.isNotBlank(bo.getLogoUrl()), CategoryFrontend::getLogoUrl, bo.getLogoUrl());
        lqw.eq(bo.getIsHidden() != null, CategoryFrontend::getIsHidden, bo.getIsHidden());
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(CategoryFrontendBo bo) {
        CategoryFrontend add = MapstructUtils.convert(bo, CategoryFrontend.class);
        int level = validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            syncFrontBackRel(add.getId(), level, bo.getBackendCategoryIds());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(CategoryFrontendBo bo) {
        CategoryFrontend update = MapstructUtils.convert(bo, CategoryFrontend.class);
        int level = validEntityBeforeSave(update);
        boolean flag = baseMapper.updateById(update) > 0;
        if (flag) {
            syncFrontBackRel(update.getId(), level, bo.getBackendCategoryIds());
        }
        return flag;
    }

    private int validEntityBeforeSave(CategoryFrontend entity) {
        if (entity.getParentId() == null) {
            entity.setParentId(0L);
        }
        int level = resolveLevel(entity.getParentId());
        if (level > MAX_LEVEL) {
            throw new ServiceException("分类最多支持三级");
        }
        if (level < MAX_LEVEL) {
            entity.setLogoUrl(null);
        }
        if (entity.getId() != null && entity.getId().equals(entity.getParentId())) {
            throw new ServiceException("父分类不能是自己");
        }
        return level;
    }

    /**
     * 三级前台分类同步前后台映射；非三级清空映射。
     * backendCategoryIds 为 null 时表示本次不改映射（更新场景）。
     */
    private void syncFrontBackRel(Long frontendCategoryId, int level, List<Long> backendCategoryIds) {
        if (level != MAX_LEVEL) {
            frontBackRelMapper.delete(Wrappers.lambdaQuery(CategoryFrontBackRel.class)
                .eq(CategoryFrontBackRel::getFrontendCategoryId, frontendCategoryId));
            return;
        }
        if (backendCategoryIds == null) {
            return;
        }
        frontBackRelMapper.delete(Wrappers.lambdaQuery(CategoryFrontBackRel.class)
            .eq(CategoryFrontBackRel::getFrontendCategoryId, frontendCategoryId));
        if (CollUtil.isEmpty(backendCategoryIds)) {
            return;
        }
        List<Long> distinctIds = backendCategoryIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        for (Long backendId : distinctIds) {
            CategoryBackend backend = categoryBackendMapper.selectById(backendId);
            if (backend == null) {
                throw new ServiceException("后台分类不存在: " + backendId);
            }
            if (categoryBackendService.resolveLevel(backend.getParentId()) != MAX_LEVEL) {
                throw new ServiceException("仅可关联后台三级分类");
            }
            if (backend.getTypeId() == null) {
                throw new ServiceException("后台三级分类未关联商品类型: " + backendId);
            }
            CategoryFrontBackRel rel = new CategoryFrontBackRel();
            rel.setId(IdGeneratorUtil.nextLongId());
            rel.setFrontendCategoryId(frontendCategoryId);
            rel.setBackendCategoryId(backendId);
            frontBackRelMapper.insert(rel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid)) {
            Long childCount = baseMapper.selectCount(Wrappers.lambdaQuery(CategoryFrontend.class)
                .in(CategoryFrontend::getParentId, ids));
            if (childCount != null && childCount > 0) {
                throw new ServiceException("存在子分类，不允许删除");
            }
        }
        frontBackRelMapper.delete(Wrappers.lambdaQuery(CategoryFrontBackRel.class)
            .in(CategoryFrontBackRel::getFrontendCategoryId, ids));
        return baseMapper.deleteByIds(ids) > 0;
    }
}
