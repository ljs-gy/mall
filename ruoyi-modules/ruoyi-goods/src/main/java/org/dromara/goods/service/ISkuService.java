package org.dromara.goods.service;

import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.vo.SkuVo;
import org.dromara.goods.domain.bo.SkuBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品货品SKUService接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ISkuService {

    /**
     * 查询商品货品SKU
     *
     * @param id 主键
     * @return 商品货品SKU
     */
    SkuVo queryById(Long id);

    /**
     * 分页查询商品货品SKU列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品货品SKU分页列表
     */
    TableDataInfo<SkuVo> queryPageList(SkuBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品货品SKU列表
     *
     * @param bo 查询条件
     * @return 商品货品SKU列表
     */
    List<SkuVo> queryList(SkuBo bo);

    /**
     * 新增商品货品SKU
     *
     * @param bo 商品货品SKU
     * @return 是否新增成功
     */
    Boolean insertByBo(SkuBo bo);

    /**
     * 修改商品货品SKU
     *
     * @param bo 商品货品SKU
     * @return 是否修改成功
     */
    Boolean updateByBo(SkuBo bo);

    /**
     * 校验并批量删除商品货品SKU信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
