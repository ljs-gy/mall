package org.dromara.order.service;

import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.domain.vo.AfterSaleItemVo;
import org.dromara.order.domain.bo.AfterSaleItemBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 售后单明细Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IAfterSaleItemService {

    /**
     * 查询售后单明细
     *
     * @param id 主键
     * @return 售后单明细
     */
    AfterSaleItemVo queryById(Long id);

    /**
     * 分页查询售后单明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 售后单明细分页列表
     */
    TableDataInfo<AfterSaleItemVo> queryPageList(AfterSaleItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的售后单明细列表
     *
     * @param bo 查询条件
     * @return 售后单明细列表
     */
    List<AfterSaleItemVo> queryList(AfterSaleItemBo bo);

    /**
     * 新增售后单明细
     *
     * @param bo 售后单明细
     * @return 是否新增成功
     */
    Boolean insertByBo(AfterSaleItemBo bo);

    /**
     * 修改售后单明细
     *
     * @param bo 售后单明细
     * @return 是否修改成功
     */
    Boolean updateByBo(AfterSaleItemBo bo);

    /**
     * 校验并批量删除售后单明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
