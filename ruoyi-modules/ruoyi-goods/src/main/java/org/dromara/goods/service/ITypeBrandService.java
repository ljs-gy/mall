package org.dromara.goods.service;

import org.dromara.goods.domain.TypeBrand;
import org.dromara.goods.domain.vo.TypeBrandVo;
import org.dromara.goods.domain.bo.TypeBrandBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品类型与品牌关系Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ITypeBrandService {

    /**
     * 查询商品类型与品牌关系
     *
     * @param id 主键
     * @return 商品类型与品牌关系
     */
    TypeBrandVo queryById(Long id);

    /**
     * 分页查询商品类型与品牌关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品类型与品牌关系分页列表
     */
    TableDataInfo<TypeBrandVo> queryPageList(TypeBrandBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品类型与品牌关系列表
     *
     * @param bo 查询条件
     * @return 商品类型与品牌关系列表
     */
    List<TypeBrandVo> queryList(TypeBrandBo bo);

    /**
     * 新增商品类型与品牌关系
     *
     * @param bo 商品类型与品牌关系
     * @return 是否新增成功
     */
    Boolean insertByBo(TypeBrandBo bo);

    /**
     * 修改商品类型与品牌关系
     *
     * @param bo 商品类型与品牌关系
     * @return 是否修改成功
     */
    Boolean updateByBo(TypeBrandBo bo);

    /**
     * 校验并批量删除商品类型与品牌关系信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
