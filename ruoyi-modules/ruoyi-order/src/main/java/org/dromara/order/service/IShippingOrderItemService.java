package org.dromara.order.service;

import org.dromara.order.domain.ShippingOrderItem;
import org.dromara.order.domain.vo.ShippingOrderItemVo;
import org.dromara.order.domain.bo.ShippingOrderItemBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 发货单包裹明细Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IShippingOrderItemService {

    /**
     * 查询发货单包裹明细
     *
     * @param id 主键
     * @return 发货单包裹明细
     */
    ShippingOrderItemVo queryById(Long id);

    /**
     * 分页查询发货单包裹明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 发货单包裹明细分页列表
     */
    TableDataInfo<ShippingOrderItemVo> queryPageList(ShippingOrderItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的发货单包裹明细列表
     *
     * @param bo 查询条件
     * @return 发货单包裹明细列表
     */
    List<ShippingOrderItemVo> queryList(ShippingOrderItemBo bo);

    /**
     * 新增发货单包裹明细
     *
     * @param bo 发货单包裹明细
     * @return 是否新增成功
     */
    Boolean insertByBo(ShippingOrderItemBo bo);

    /**
     * 修改发货单包裹明细
     *
     * @param bo 发货单包裹明细
     * @return 是否修改成功
     */
    Boolean updateByBo(ShippingOrderItemBo bo);

    /**
     * 校验并批量删除发货单包裹明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
