package org.dromara.user.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.user.domain.bo.AccountBo;
import org.dromara.user.domain.vo.AccountVo;
import org.dromara.user.domain.Account;
import org.dromara.user.mapper.AccountMapper;
import org.dromara.user.service.IAccountService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户资金账户Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements IAccountService {

    private final AccountMapper baseMapper;

    /**
     * 查询用户资金账户
     *
     * @param id 主键
     * @return 用户资金账户
     */
    @Override
    public AccountVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户资金账户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户资金账户分页列表
     */
    @Override
    public TableDataInfo<AccountVo> queryPageList(AccountBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Account> lqw = buildQueryWrapper(bo);
        Page<AccountVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户资金账户列表
     *
     * @param bo 查询条件
     * @return 用户资金账户列表
     */
    @Override
    public List<AccountVo> queryList(AccountBo bo) {
        LambdaQueryWrapper<Account> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Account> buildQueryWrapper(AccountBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Account> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Account::getId);
        lqw.eq(bo.getUserinfoId() != null, Account::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getAccountNo()), Account::getAccountNo, bo.getAccountNo());
        lqw.like(StringUtils.isNotBlank(bo.getAccountName()), Account::getAccountName, bo.getAccountName());
        lqw.eq(StringUtils.isNotBlank(bo.getAccountType()), Account::getAccountType, bo.getAccountType());
        lqw.eq(StringUtils.isNotBlank(bo.getCurrency()), Account::getCurrency, bo.getCurrency());
        lqw.eq(bo.getBalance() != null, Account::getBalance, bo.getBalance());
        lqw.eq(bo.getAvailableBalance() != null, Account::getAvailableBalance, bo.getAvailableBalance());
        lqw.eq(bo.getFrozenBalance() != null, Account::getFrozenBalance, bo.getFrozenBalance());
        lqw.eq(bo.getStatus() != null, Account::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增用户资金账户
     *
     * @param bo 用户资金账户
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AccountBo bo) {
        Account add = MapstructUtils.convert(bo, Account.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户资金账户
     *
     * @param bo 用户资金账户
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AccountBo bo) {
        Account update = MapstructUtils.convert(bo, Account.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Account entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户资金账户信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
