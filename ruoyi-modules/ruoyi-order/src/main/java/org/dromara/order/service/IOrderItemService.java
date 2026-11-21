package org.dromara.order.service;

import org.dromara.order.domain.OrderItem;
import org.dromara.order.domain.vo.OrderItemVo;
import org.dromara.order.domain.bo.OrderItemBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 订单明细Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IOrderItemService {

    /**
     * 查询订单明细
     *
     * @param id 主键
     * @return 订单明细
     */
    OrderItemVo queryById(Long id);

    /**
     * 分页查询订单明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单明细分页列表
     */
    TableDataInfo<OrderItemVo> queryPageList(OrderItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的订单明细列表
     *
     * @param bo 查询条件
     * @return 订单明细列表
     */
    List<OrderItemVo> queryList(OrderItemBo bo);

    /**
     * 新增订单明细
     *
     * @param bo 订单明细
     * @return 是否新增成功
     */
    Boolean insertByBo(OrderItemBo bo);

    /**
     * 修改订单明细
     *
     * @param bo 订单明细
     * @return 是否修改成功
     */
    Boolean updateByBo(OrderItemBo bo);

    /**
     * 校验并批量删除订单明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
