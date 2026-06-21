package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.BidLogVo;
import org.dromara.bidding.domain.bo.BidLogBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 出价流水Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IBidLogService {

    /**
     * 查询出价流水
     *
     * @param id 主键
     * @return 出价流水
     */
    BidLogVo queryById(Long id);

    /**
     * 分页查询出价流水列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 出价流水分页列表
     */
    TableDataInfo<BidLogVo> queryPageList(BidLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的出价流水列表
     *
     * @param bo 查询条件
     * @return 出价流水列表
     */
    List<BidLogVo> queryList(BidLogBo bo);

    /**
     * 新增出价流水
     *
     * @param bo 出价流水
     * @return 是否新增成功
     */
    Boolean insertByBo(BidLogBo bo);

    /**
     * 修改出价流水
     *
     * @param bo 出价流水
     * @return 是否修改成功
     */
    Boolean updateByBo(BidLogBo bo);

    /**
     * 校验并批量删除出价流水信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
