package org.dromara.bidding.service.impl;

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
import org.dromara.bidding.domain.bo.UserDepositLogBo;
import org.dromara.bidding.domain.vo.UserDepositLogVo;
import org.dromara.bidding.domain.UserDepositLog;
import org.dromara.bidding.mapper.UserDepositLogMapper;
import org.dromara.bidding.service.IUserDepositLogService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 保证金流水Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserDepositLogServiceImpl implements IUserDepositLogService {

    private final UserDepositLogMapper baseMapper;

    @Override
    public UserDepositLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<UserDepositLogVo> queryPageList(UserDepositLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<UserDepositLog> lqw = buildQueryWrapper(bo);
        Page<UserDepositLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<UserDepositLogVo> queryList(UserDepositLogBo bo) {
        LambdaQueryWrapper<UserDepositLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<UserDepositLog> buildQueryWrapper(UserDepositLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<UserDepositLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getLogCode()), UserDepositLog::getLogCode, bo.getLogCode());
        lqw.like(StringUtils.isNotBlank(bo.getBizNo()), UserDepositLog::getBizNo, bo.getBizNo());
        lqw.eq(bo.getUserId() != null, UserDepositLog::getUserId, bo.getUserId());
        lqw.eq(bo.getSessionId() != null, UserDepositLog::getSessionId, bo.getSessionId());
        lqw.eq(bo.getItemId() != null, UserDepositLog::getItemId, bo.getItemId());
        lqw.eq(bo.getDirection() != null, UserDepositLog::getDirection, bo.getDirection());
        return lqw;
    }

    @Override
    public Boolean insertByBo(UserDepositLogBo bo) {
        UserDepositLog add = MapstructUtils.convert(bo, UserDepositLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(UserDepositLogBo bo) {
        UserDepositLog update = MapstructUtils.convert(bo, UserDepositLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(UserDepositLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
