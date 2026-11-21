package org.dromara.order.service;

import org.dromara.order.domain.ShippingOrder;
import org.dromara.order.domain.vo.ShippingOrderVo;
import org.dromara.order.domain.bo.ShippingOrderBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 发货单Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IShippingOrderService {

    /**
     * 查询发货单
     *
     * @param id 主键
     * @return 发货单
     */
    ShippingOrderVo queryById(Long id);

    /**
     * 分页查询发货单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 发货单分页列表
     */
    TableDataInfo<ShippingOrderVo> queryPageList(ShippingOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的发货单列表
     *
     * @param bo 查询条件
     * @return 发货单列表
     */
    List<ShippingOrderVo> queryList(ShippingOrderBo bo);

    /**
     * 新增发货单
     *
     * @param bo 发货单
     * @return 是否新增成功
     */
    Boolean insertByBo(ShippingOrderBo bo);

    /**
     * 修改发货单
     *
     * @param bo 发货单
     * @return 是否修改成功
     */
    Boolean updateByBo(ShippingOrderBo bo);

    /**
     * 校验并批量删除发货单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
