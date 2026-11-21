package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.BidOrderVo;
import org.dromara.bidding.domain.bo.BidOrderBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 竞价订单Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IBidOrderService {

    /**
     * 查询竞价订单
     *
     * @param id 主键
     * @return 竞价订单
     */
    BidOrderVo queryById(Long id);

    /**
     * 分页查询竞价订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 竞价订单分页列表
     */
    TableDataInfo<BidOrderVo> queryPageList(BidOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的竞价订单列表
     *
     * @param bo 查询条件
     * @return 竞价订单列表
     */
    List<BidOrderVo> queryList(BidOrderBo bo);

    /**
     * 新增竞价订单
     *
     * @param bo 竞价订单
     * @return 是否新增成功
     */
    Boolean insertByBo(BidOrderBo bo);

    /**
     * 修改竞价订单
     *
     * @param bo 竞价订单
     * @return 是否修改成功
     */
    Boolean updateByBo(BidOrderBo bo);

    /**
     * 校验并批量删除竞价订单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 截拍落槌后根据中标标的生成竞价订单
     *
     * @param itemId 标的ID
     */
    void generateStrikeOrder(Long itemId);
}
