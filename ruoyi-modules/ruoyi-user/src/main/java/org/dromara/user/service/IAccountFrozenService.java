package org.dromara.user.service;

import org.dromara.user.domain.vo.AccountFrozenVo;
import org.dromara.user.domain.bo.AccountFrozenBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户资金账户冻结明细Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IAccountFrozenService {

    /**
     * 查询用户资金账户冻结明细
     */
    AccountFrozenVo queryById(Long id);

    /**
     * 分页查询用户资金账户冻结明细列表
     */
    TableDataInfo<AccountFrozenVo> queryPageList(AccountFrozenBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户资金账户冻结明细列表
     */
    List<AccountFrozenVo> queryList(AccountFrozenBo bo);

    /**
     * 新增用户资金账户冻结明细
     */
    Boolean insertByBo(AccountFrozenBo bo);

    /**
     * 修改用户资金账户冻结明细
     */
    Boolean updateByBo(AccountFrozenBo bo);

    /**
     * 校验并批量删除用户资金账户冻结明细
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
