package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.UserDepositLogVo;
import org.dromara.bidding.domain.bo.UserDepositLogBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 保证金流水Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IUserDepositLogService {

    /**
     * 查询保证金流水
     *
     * @param id 主键
     * @return 保证金流水
     */
    UserDepositLogVo queryById(Long id);

    /**
     * 分页查询保证金流水列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 保证金流水分页列表
     */
    TableDataInfo<UserDepositLogVo> queryPageList(UserDepositLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的保证金流水列表
     *
     * @param bo 查询条件
     * @return 保证金流水列表
     */
    List<UserDepositLogVo> queryList(UserDepositLogBo bo);

    /**
     * 新增保证金流水
     *
     * @param bo 保证金流水
     * @return 是否新增成功
     */
    Boolean insertByBo(UserDepositLogBo bo);

    /**
     * 修改保证金流水
     *
     * @param bo 保证金流水
     * @return 是否修改成功
     */
    Boolean updateByBo(UserDepositLogBo bo);

    /**
     * 校验并批量删除保证金流水信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
