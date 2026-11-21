package org.dromara.goods.service;

import org.dromara.goods.domain.Attribute;
import org.dromara.goods.domain.vo.AttributeVo;
import org.dromara.goods.domain.bo.AttributeBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品属性Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface IAttributeService {

    /**
     * 查询商品属性
     *
     * @param id 主键
     * @return 商品属性
     */
    AttributeVo queryById(Long id);

    /**
     * 分页查询商品属性列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品属性分页列表
     */
    TableDataInfo<AttributeVo> queryPageList(AttributeBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品属性列表
     *
     * @param bo 查询条件
     * @return 商品属性列表
     */
    List<AttributeVo> queryList(AttributeBo bo);

    /**
     * 新增商品属性
     *
     * @param bo 商品属性
     * @return 是否新增成功
     */
    Boolean insertByBo(AttributeBo bo);

    /**
     * 修改商品属性
     *
     * @param bo 商品属性
     * @return 是否修改成功
     */
    Boolean updateByBo(AttributeBo bo);

    /**
     * 校验并批量删除商品属性信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
