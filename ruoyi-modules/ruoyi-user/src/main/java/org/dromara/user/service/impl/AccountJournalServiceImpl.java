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
import org.dromara.user.domain.bo.AccountJournalBo;
import org.dromara.user.domain.vo.AccountJournalVo;
import org.dromara.user.domain.AccountJournal;
import org.dromara.user.mapper.AccountJournalMapper;
import org.dromara.user.service.IAccountJournalService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户账务动账流水Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountJournalServiceImpl implements IAccountJournalService {

    private final AccountJournalMapper baseMapper;

    /**
     * 查询用户账务动账流水
     *
     * @param id 主键
     * @return 用户账务动账流水
     */
    @Override
    public AccountJournalVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户账务动账流水列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户账务动账流水分页列表
     */
    @Override
    public TableDataInfo<AccountJournalVo> queryPageList(AccountJournalBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AccountJournal> lqw = buildQueryWrapper(bo);
        Page<AccountJournalVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户账务动账流水列表
     *
     * @param bo 查询条件
     * @return 用户账务动账流水列表
     */
    @Override
    public List<AccountJournalVo> queryList(AccountJournalBo bo) {
        LambdaQueryWrapper<AccountJournal> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AccountJournal> buildQueryWrapper(AccountJournalBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AccountJournal> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AccountJournal::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getJournalNo()), AccountJournal::getJournalNo, bo.getJournalNo());
        lqw.eq(StringUtils.isNotBlank(bo.getAccountNo()), AccountJournal::getAccountNo, bo.getAccountNo());
        lqw.eq(StringUtils.isNotBlank(bo.getBizType()), AccountJournal::getBizType, bo.getBizType());
        lqw.eq(StringUtils.isNotBlank(bo.getBizOrderNo()), AccountJournal::getBizOrderNo, bo.getBizOrderNo());
        lqw.eq(bo.getDirection() != null, AccountJournal::getDirection, bo.getDirection());
        lqw.eq(bo.getAmount() != null, AccountJournal::getAmount, bo.getAmount());
        lqw.eq(bo.getBeforeBalance() != null, AccountJournal::getBeforeBalance, bo.getBeforeBalance());
        lqw.eq(bo.getAfterBalance() != null, AccountJournal::getAfterBalance, bo.getAfterBalance());
        return lqw;
    }

    /**
     * 新增用户账务动账流水
     *
     * @param bo 用户账务动账流水
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AccountJournalBo bo) {
        AccountJournal add = MapstructUtils.convert(bo, AccountJournal.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户账务动账流水
     *
     * @param bo 用户账务动账流水
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AccountJournalBo bo) {
        AccountJournal update = MapstructUtils.convert(bo, AccountJournal.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AccountJournal entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户账务动账流水信息
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
