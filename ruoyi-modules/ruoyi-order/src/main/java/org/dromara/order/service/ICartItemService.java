package org.dromara.order.service;

import org.dromara.order.domain.CartItem;
import org.dromara.order.domain.vo.CartItemVo;
import org.dromara.order.domain.bo.CartItemBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 购物车明细Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface ICartItemService {

    /**
     * 查询购物车明细
     *
     * @param id 主键
     * @return 购物车明细
     */
    CartItemVo queryById(Long id);

    /**
     * 分页查询购物车明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 购物车明细分页列表
     */
    TableDataInfo<CartItemVo> queryPageList(CartItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的购物车明细列表
     *
     * @param bo 查询条件
     * @return 购物车明细列表
     */
    List<CartItemVo> queryList(CartItemBo bo);

    /**
     * 新增购物车明细
     *
     * @param bo 购物车明细
     * @return 是否新增成功
     */
    Boolean insertByBo(CartItemBo bo);

    /**
     * 修改购物车明细
     *
     * @param bo 购物车明细
     * @return 是否修改成功
     */
    Boolean updateByBo(CartItemBo bo);

    /**
     * 校验并批量删除购物车明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
