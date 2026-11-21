package org.dromara.user.service;

import org.dromara.user.domain.RechargeApply;
import org.dromara.user.domain.vo.RechargeApplyVo;
import org.dromara.user.domain.bo.RechargeApplyBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 充值申请Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IRechargeApplyService {

    /**
     * 查询充值申请
     *
     * @param id 主键
     * @return 充值申请
     */
    RechargeApplyVo queryById(Long id);

    /**
     * 分页查询充值申请列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 充值申请分页列表
     */
    TableDataInfo<RechargeApplyVo> queryPageList(RechargeApplyBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的充值申请列表
     *
     * @param bo 查询条件
     * @return 充值申请列表
     */
    List<RechargeApplyVo> queryList(RechargeApplyBo bo);

    /**
     * 新增充值申请
     *
     * @param bo 充值申请
     * @return 是否新增成功
     */
    Boolean insertByBo(RechargeApplyBo bo);

    /**
     * 修改充值申请
     *
     * @param bo 充值申请
     * @return 是否修改成功
     */
    Boolean updateByBo(RechargeApplyBo bo);

    /**
     * 校验并批量删除充值申请信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
