package org.dromara.order.service;

import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.bo.AfterSaleBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 售后退货单Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IAfterSaleService {

    /**
     * 查询售后退货单
     *
     * @param id 主键
     * @return 售后退货单
     */
    AfterSaleVo queryById(Long id);

    /**
     * 分页查询售后退货单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 售后退货单分页列表
     */
    TableDataInfo<AfterSaleVo> queryPageList(AfterSaleBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的售后退货单列表
     *
     * @param bo 查询条件
     * @return 售后退货单列表
     */
    List<AfterSaleVo> queryList(AfterSaleBo bo);

    /**
     * 新增售后退货单
     *
     * @param bo 售后退货单
     * @return 是否新增成功
     */
    Boolean insertByBo(AfterSaleBo bo);

    /**
     * 修改售后退货单
     *
     * @param bo 售后退货单
     * @return 是否修改成功
     */
    Boolean updateByBo(AfterSaleBo bo);

    /**
     * 校验并批量删除售后退货单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
