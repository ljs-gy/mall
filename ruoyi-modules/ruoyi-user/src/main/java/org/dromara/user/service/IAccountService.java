package org.dromara.user.service;

import org.dromara.user.domain.Account;
import org.dromara.user.domain.vo.AccountVo;
import org.dromara.user.domain.bo.AccountBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户资金账户Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IAccountService {

    /**
     * 查询用户资金账户
     *
     * @param id 主键
     * @return 用户资金账户
     */
    AccountVo queryById(Long id);

    /**
     * 分页查询用户资金账户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户资金账户分页列表
     */
    TableDataInfo<AccountVo> queryPageList(AccountBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户资金账户列表
     *
     * @param bo 查询条件
     * @return 用户资金账户列表
     */
    List<AccountVo> queryList(AccountBo bo);

    /**
     * 新增用户资金账户
     *
     * @param bo 用户资金账户
     * @return 是否新增成功
     */
    Boolean insertByBo(AccountBo bo);

    /**
     * 修改用户资金账户
     *
     * @param bo 用户资金账户
     * @return 是否修改成功
     */
    Boolean updateByBo(AccountBo bo);

    /**
     * 校验并批量删除用户资金账户信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
