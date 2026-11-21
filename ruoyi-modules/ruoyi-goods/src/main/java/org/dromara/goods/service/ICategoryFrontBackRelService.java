package org.dromara.goods.service;

import org.dromara.goods.domain.vo.CategoryFrontBackRelVo;
import org.dromara.goods.domain.bo.CategoryFrontBackRelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 前后台分类映射关系Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ICategoryFrontBackRelService {

    CategoryFrontBackRelVo queryById(Long id);

    TableDataInfo<CategoryFrontBackRelVo> queryPageList(CategoryFrontBackRelBo bo, PageQuery pageQuery);

    List<CategoryFrontBackRelVo> queryList(CategoryFrontBackRelBo bo);

    Boolean insertByBo(CategoryFrontBackRelBo bo);

    Boolean updateByBo(CategoryFrontBackRelBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
