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
import org.dromara.user.domain.bo.AccountFrozenBo;
import org.dromara.user.domain.vo.AccountFrozenVo;
import org.dromara.user.domain.AccountFrozen;
import org.dromara.user.mapper.AccountFrozenMapper;
import org.dromara.user.service.IAccountFrozenService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户资金账户冻结明细Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AccountFrozenServiceImpl implements IAccountFrozenService {

    private final AccountFrozenMapper baseMapper;

    @Override
    public AccountFrozenVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<AccountFrozenVo> queryPageList(AccountFrozenBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AccountFrozen> lqw = buildQueryWrapper(bo);
        Page<AccountFrozenVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<AccountFrozenVo> queryList(AccountFrozenBo bo) {
        LambdaQueryWrapper<AccountFrozen> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AccountFrozen> buildQueryWrapper(AccountFrozenBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AccountFrozen> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(AccountFrozen::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getAccountNo()), AccountFrozen::getAccountNo, bo.getAccountNo());
        lqw.eq(StringUtils.isNotBlank(bo.getBizType()), AccountFrozen::getBizType, bo.getBizType());
        lqw.eq(StringUtils.isNotBlank(bo.getBizOrderNo()), AccountFrozen::getBizOrderNo, bo.getBizOrderNo());
        lqw.eq(bo.getStatus() != null, AccountFrozen::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getUnfrozenBizNo()), AccountFrozen::getUnfrozenBizNo, bo.getUnfrozenBizNo());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            AccountFrozen::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        return lqw;
    }

    @Override
    public Boolean insertByBo(AccountFrozenBo bo) {
        AccountFrozen add = MapstructUtils.convert(bo, AccountFrozen.class);
        if (add.getUnfrozenAmount() == null) {
            add.setUnfrozenAmount(BigDecimal.ZERO);
        }
        if (add.getStatus() == null) {
            add.setStatus(1L);
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(AccountFrozenBo bo) {
        AccountFrozen update = MapstructUtils.convert(bo, AccountFrozen.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}
