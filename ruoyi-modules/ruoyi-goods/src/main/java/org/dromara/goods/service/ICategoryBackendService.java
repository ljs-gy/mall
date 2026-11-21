package org.dromara.goods.service;

import cn.hutool.core.lang.tree.Tree;
import org.dromara.goods.domain.vo.CategoryBackendVo;
import org.dromara.goods.domain.bo.CategoryBackendBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 后台商品分类Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ICategoryBackendService {

    /**
     * 查询后台商品分类
     *
     * @param id 主键
     * @return 后台商品分类
     */
    CategoryBackendVo queryById(Long id);

    /**
     * 分页查询后台商品分类列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 后台商品分类分页列表
     */
    TableDataInfo<CategoryBackendVo> queryPageList(CategoryBackendBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的后台商品分类列表
     *
     * @param bo 查询条件
     * @return 后台商品分类列表
     */
    List<CategoryBackendVo> queryList(CategoryBackendBo bo);

    /**
     * 新增后台商品分类
     *
     * @param bo 后台商品分类
     * @return 是否新增成功
     */
    Boolean insertByBo(CategoryBackendBo bo);

    /**
     * 修改后台商品分类
     *
     * @param bo 后台商品分类
     * @return 是否修改成功
     */
    Boolean updateByBo(CategoryBackendBo bo);

    /**
     * 校验并批量删除后台商品分类信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 后台分类树
     */
    List<Tree<Long>> selectTree();

    /**
     * 计算分类层级（根为1）
     */
    int resolveLevel(Long parentId);
}
