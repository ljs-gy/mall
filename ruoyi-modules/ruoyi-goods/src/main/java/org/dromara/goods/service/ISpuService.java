package org.dromara.goods.service;

import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.vo.SpuVo;
import org.dromara.goods.domain.bo.SpuBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品SPU主Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ISpuService {

    /**
     * 查询商品SPU主
     *
     * @param id 主键
     * @return 商品SPU主
     */
    SpuVo queryById(Long id);

    /**
     * 分页查询商品SPU主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品SPU主分页列表
     */
    TableDataInfo<SpuVo> queryPageList(SpuBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品SPU主列表
     *
     * @param bo 查询条件
     * @return 商品SPU主列表
     */
    List<SpuVo> queryList(SpuBo bo);

    /**
     * 新增商品SPU主
     *
     * @param bo 商品SPU主
     * @return 是否新增成功
     */
    Boolean insertByBo(SpuBo bo);

    /**
     * 修改商品SPU主
     *
     * @param bo 商品SPU主
     * @return 是否修改成功
     */
    Boolean updateByBo(SpuBo bo);

    /**
     * 校验并批量删除商品SPU主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
