package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.ApplyItemRelVo;
import org.dromara.bidding.domain.bo.ApplyItemRelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 报名标的关联Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IApplyItemRelService {

    /**
     * 查询报名标的关联
     *
     * @param id 主键
     * @return 报名标的关联
     */
    ApplyItemRelVo queryById(Long id);

    /**
     * 分页查询报名标的关联列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 报名标的关联分页列表
     */
    TableDataInfo<ApplyItemRelVo> queryPageList(ApplyItemRelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的报名标的关联列表
     *
     * @param bo 查询条件
     * @return 报名标的关联列表
     */
    List<ApplyItemRelVo> queryList(ApplyItemRelBo bo);

    /**
     * 新增报名标的关联
     *
     * @param bo 报名标的关联
     * @return 是否新增成功
     */
    Boolean insertByBo(ApplyItemRelBo bo);

    /**
     * 修改报名标的关联
     *
     * @param bo 报名标的关联
     * @return 是否修改成功
     */
    Boolean updateByBo(ApplyItemRelBo bo);

    /**
     * 校验并批量删除报名标的关联信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
