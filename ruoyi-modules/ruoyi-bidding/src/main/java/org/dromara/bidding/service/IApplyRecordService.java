package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.ApplyRecordVo;
import org.dromara.bidding.domain.bo.ApplyRecordBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 报名记录Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IApplyRecordService {

    /**
     * 查询报名记录
     *
     * @param id 主键
     * @return 报名记录
     */
    ApplyRecordVo queryById(Long id);

    /**
     * 分页查询报名记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 报名记录分页列表
     */
    TableDataInfo<ApplyRecordVo> queryPageList(ApplyRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的报名记录列表
     *
     * @param bo 查询条件
     * @return 报名记录列表
     */
    List<ApplyRecordVo> queryList(ApplyRecordBo bo);

    /**
     * 新增报名记录
     *
     * @param bo 报名记录
     * @return 是否新增成功
     */
    Boolean insertByBo(ApplyRecordBo bo);

    /**
     * 修改报名记录
     *
     * @param bo 报名记录
     * @return 是否修改成功
     */
    Boolean updateByBo(ApplyRecordBo bo);

    /**
     * 校验并批量删除报名记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
