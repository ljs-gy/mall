package org.dromara.order.service;

import org.dromara.order.domain.Order;
import org.dromara.order.domain.vo.OrderVo;
import org.dromara.order.domain.bo.OrderBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 订单主Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IOrderService {

    /**
     * 查询订单主
     *
     * @param id 主键
     * @return 订单主
     */
    OrderVo queryById(Long id);

    /**
     * 分页查询订单主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单主分页列表
     */
    TableDataInfo<OrderVo> queryPageList(OrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的订单主列表
     *
     * @param bo 查询条件
     * @return 订单主列表
     */
    List<OrderVo> queryList(OrderBo bo);

    /**
     * 新增订单主
     *
     * @param bo 订单主
     * @return 是否新增成功
     */
    Boolean insertByBo(OrderBo bo);

    /**
     * 修改订单主
     *
     * @param bo 订单主
     * @return 是否修改成功
     */
    Boolean updateByBo(OrderBo bo);

    /**
     * 校验并批量删除订单主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
