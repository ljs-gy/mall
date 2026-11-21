package org.dromara.order.service;

import org.dromara.order.domain.OrderTimeConfig;
import org.dromara.order.domain.vo.OrderTimeConfigVo;
import org.dromara.order.domain.bo.OrderTimeConfigBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 订单交易时效配置Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IOrderTimeConfigService {

    /**
     * 查询订单交易时效配置
     *
     * @param id 主键
     * @return 订单交易时效配置
     */
    OrderTimeConfigVo queryById(Long id);

    /**
     * 分页查询订单交易时效配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单交易时效配置分页列表
     */
    TableDataInfo<OrderTimeConfigVo> queryPageList(OrderTimeConfigBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的订单交易时效配置列表
     *
     * @param bo 查询条件
     * @return 订单交易时效配置列表
     */
    List<OrderTimeConfigVo> queryList(OrderTimeConfigBo bo);

    /**
     * 新增订单交易时效配置
     *
     * @param bo 订单交易时效配置
     * @return 是否新增成功
     */
    Boolean insertByBo(OrderTimeConfigBo bo);

    /**
     * 修改订单交易时效配置
     *
     * @param bo 订单交易时效配置
     * @return 是否修改成功
     */
    Boolean updateByBo(OrderTimeConfigBo bo);

    /**
     * 校验并批量删除订单交易时效配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
