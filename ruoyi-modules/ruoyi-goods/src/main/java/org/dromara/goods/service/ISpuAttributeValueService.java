package org.dromara.goods.service;

import org.dromara.goods.domain.SpuAttributeValue;
import org.dromara.goods.domain.vo.SpuAttributeValueVo;
import org.dromara.goods.domain.bo.SpuAttributeValueBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品SPU属性值Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ISpuAttributeValueService {

    /**
     * 查询商品SPU属性值
     *
     * @param id 主键
     * @return 商品SPU属性值
     */
    SpuAttributeValueVo queryById(Long id);

    /**
     * 分页查询商品SPU属性值列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品SPU属性值分页列表
     */
    TableDataInfo<SpuAttributeValueVo> queryPageList(SpuAttributeValueBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品SPU属性值列表
     *
     * @param bo 查询条件
     * @return 商品SPU属性值列表
     */
    List<SpuAttributeValueVo> queryList(SpuAttributeValueBo bo);

    /**
     * 新增商品SPU属性值
     *
     * @param bo 商品SPU属性值
     * @return 是否新增成功
     */
    Boolean insertByBo(SpuAttributeValueBo bo);

    /**
     * 修改商品SPU属性值
     *
     * @param bo 商品SPU属性值
     * @return 是否修改成功
     */
    Boolean updateByBo(SpuAttributeValueBo bo);

    /**
     * 校验并批量删除商品SPU属性值信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
