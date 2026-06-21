package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.FinancialApplyVo;
import org.dromara.bidding.domain.bo.FinancialApplyBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 清算申请Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IFinancialApplyService {

    /**
     * 查询清算申请
     *
     * @param id 主键
     * @return 清算申请
     */
    FinancialApplyVo queryById(Long id);

    /**
     * 分页查询清算申请列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 清算申请分页列表
     */
    TableDataInfo<FinancialApplyVo> queryPageList(FinancialApplyBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的清算申请列表
     *
     * @param bo 查询条件
     * @return 清算申请列表
     */
    List<FinancialApplyVo> queryList(FinancialApplyBo bo);

    /**
     * 新增清算申请
     *
     * @param bo 清算申请
     * @return 是否新增成功
     */
    Boolean insertByBo(FinancialApplyBo bo);

    /**
     * 修改清算申请
     *
     * @param bo 清算申请
     * @return 是否修改成功
     */
    Boolean updateByBo(FinancialApplyBo bo);

    /**
     * 校验并批量删除清算申请信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
