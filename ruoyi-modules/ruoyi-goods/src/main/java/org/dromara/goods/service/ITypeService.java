package org.dromara.goods.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.goods.domain.bo.TypeBo;
import org.dromara.goods.domain.vo.TypeTemplateVo;
import org.dromara.goods.domain.vo.TypeVo;

import java.util.Collection;
import java.util.List;

/**
 * 商品类型Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ITypeService {

    TypeVo queryById(Long id);

    TableDataInfo<TypeVo> queryPageList(TypeBo bo, PageQuery pageQuery);

    List<TypeVo> queryList(TypeBo bo);

    Boolean insertByBo(TypeBo bo);

    Boolean updateByBo(TypeBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 类型下发布模板（品牌、规格组/规格/选项、参数、属性/选项）
     */
    TypeTemplateVo queryTemplate(Long typeId);
}
