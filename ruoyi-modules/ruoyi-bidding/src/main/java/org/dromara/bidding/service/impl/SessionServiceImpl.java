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
import org.dromara.bidding.domain.bo.SessionBo;
import org.dromara.bidding.domain.vo.SessionVo;
import org.dromara.bidding.domain.Session;
import org.dromara.bidding.mapper.SessionMapper;
import org.dromara.bidding.service.ISessionService;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 竞价场次Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements ISessionService {

    private final SessionMapper baseMapper;

    @Override
    public SessionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<SessionVo> queryPageList(SessionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Session> lqw = buildQueryWrapper(bo);
        Page<SessionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SessionVo> queryList(SessionBo bo) {
        LambdaQueryWrapper<Session> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Session> buildQueryWrapper(SessionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Session> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getSessionCode()), Session::getSessionCode, bo.getSessionCode());
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Session::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getWarehouseName()), Session::getWarehouseName, bo.getWarehouseName());
        lqw.eq(bo.getSessionMode() != null, Session::getSessionMode, bo.getSessionMode());
        lqw.eq(bo.getRuleId() != null, Session::getRuleId, bo.getRuleId());
        lqw.eq(bo.getDepositType() != null, Session::getDepositType, bo.getDepositType());
        lqw.eq(bo.getAllowedUserType() != null, Session::getAllowedUserType, bo.getAllowedUserType());
        lqw.eq(bo.getAuditStatus() != null, Session::getAuditStatus, bo.getAuditStatus());
        lqw.eq(bo.getBidStatus() != null, Session::getBidStatus, bo.getBidStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(SessionBo bo) {
        Session add = MapstructUtils.convert(bo, Session.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SessionBo bo) {
        Session update = MapstructUtils.convert(bo, Session.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(Session entity){
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
