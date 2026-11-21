package org.dromara.user.service;

import org.dromara.user.domain.AccountJournal;
import org.dromara.user.domain.vo.AccountJournalVo;
import org.dromara.user.domain.bo.AccountJournalBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户账务动账流水Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IAccountJournalService {

    /**
     * 查询用户账务动账流水
     *
     * @param id 主键
     * @return 用户账务动账流水
     */
    AccountJournalVo queryById(Long id);

    /**
     * 分页查询用户账务动账流水列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户账务动账流水分页列表
     */
    TableDataInfo<AccountJournalVo> queryPageList(AccountJournalBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户账务动账流水列表
     *
     * @param bo 查询条件
     * @return 用户账务动账流水列表
     */
    List<AccountJournalVo> queryList(AccountJournalBo bo);

    /**
     * 新增用户账务动账流水
     *
     * @param bo 用户账务动账流水
     * @return 是否新增成功
     */
    Boolean insertByBo(AccountJournalBo bo);

    /**
     * 修改用户账务动账流水
     *
     * @param bo 用户账务动账流水
     * @return 是否修改成功
     */
    Boolean updateByBo(AccountJournalBo bo);

    /**
     * 校验并批量删除用户账务动账流水信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
