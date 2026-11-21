package org.dromara.goods.service;

import cn.hutool.core.lang.tree.Tree;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.goods.domain.bo.CategoryFrontendBo;
import org.dromara.goods.domain.vo.CategoryBackendVo;
import org.dromara.goods.domain.vo.CategoryFrontendVo;

import java.util.Collection;
import java.util.List;

/**
 * 前台商品分类Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ICategoryFrontendService {

    CategoryFrontendVo queryById(Long id);

    TableDataInfo<CategoryFrontendVo> queryPageList(CategoryFrontendBo bo, PageQuery pageQuery);

    List<CategoryFrontendVo> queryList(CategoryFrontendBo bo);

    Boolean insertByBo(CategoryFrontendBo bo);

    Boolean updateByBo(CategoryFrontendBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 前台分类树
     */
    List<Tree<Long>> selectTree();

    /**
     * 计算分类层级（根为1）
     */
    int resolveLevel(Long parentId);

    /**
     * 三级前台分类已关联的后台三级分类列表
     */
    List<CategoryBackendVo> listBackendOptions(Long frontendCategoryId);
}
