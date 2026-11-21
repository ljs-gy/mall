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
import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.bidding.domain.bo.SessionBo;
import org.dromara.bidding.domain.vo.SessionVo;
import org.dromara.bidding.domain.Session;
import org.dromara.bidding.enums.AnnouncementItemStatus;
import org.dromara.bidding.enums.SessionAuditStatus;
import org.dromara.bidding.enums.SessionBidStatus;
import org.dromara.bidding.mapper.AnnouncementItemMapper;
import org.dromara.bidding.mapper.SessionMapper;
import org.dromara.bidding.mq.BidMqConstants;
import org.dromara.bidding.service.ISessionService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.satoken.utils.LoginHelper;
import cn.hutool.core.date.DateUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
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
    private final AnnouncementItemMapper announcementItemMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAnnouncement(Long sessionId) {
        Session update = new Session();
        update.setAuditStatus(SessionAuditStatus.APPROVED.getCode());
        update.setAuditBy(LoginHelper.getUserId());
        update.setAuditTime(new Date());
        int rows = baseMapper.update(update, Wrappers.<Session>lambdaUpdate()
            .eq(Session::getId, sessionId)
            .eq(Session::getAuditStatus, SessionAuditStatus.PENDING.getCode()));
        if (rows == 0) {
            throw new ServiceException("审批冲突：该公告已被处理，请勿重复审批！");
        }

        Session session = baseMapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException("未找到该场次活动详情！");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bidStartDateTime = DateUtil.toLocalDateTime(session.getStartTime());
        LocalDateTime bidEndDateTime = DateUtil.toLocalDateTime(session.getEndTime());
        if (session.getStartTime() == null || session.getEndTime() == null) {
            throw new ServiceException("场次开拍或截拍时间未配置！");
        }
        if (now.isAfter(bidEndDateTime)) {
            throw new ServiceException("该场次截拍时间已过，无法审核通过！");
        }

        Map<String, Object> msgPayload = new HashMap<>();
        msgPayload.put("sessionId", sessionId);
        msgPayload.put("tenantId", session.getTenantId());

        rabbitTemplate.convertAndSend(
            BidMqConstants.EXCHANGE_BUSINESS,
            BidMqConstants.KEY_ASYNC_REFRESH_CACHE,
            msgPayload
        );

        if (now.isBefore(bidStartDateTime)) {
            long ttlToStart = Duration.between(now, bidStartDateTime).toMillis();
            rabbitTemplate.convertAndSend(BidMqConstants.EXCHANGE_CLOCK, BidMqConstants.KEY_BID_START_DELAY, msgPayload, message -> {
                message.getMessageProperties().setDelayLong(ttlToStart);
                return message;
            });
            log.info("场次 {} 审核通过，{} 毫秒后自动开拍", sessionId, ttlToStart);
            return;
        }

        baseMapper.update(Wrappers.<Session>lambdaUpdate()
            .set(Session::getBidStatus, SessionBidStatus.BIDDING.getCode())
            .eq(Session::getId, sessionId));
        announcementItemMapper.update(Wrappers.<AnnouncementItem>lambdaUpdate()
            .set(AnnouncementItem::getItemStatus, AnnouncementItemStatus.BIDDING.getCode())
            .eq(AnnouncementItem::getSessionId, sessionId));

        List<Long> itemIds = announcementItemMapper.selectList(
            Wrappers.<AnnouncementItem>lambdaQuery()
                .select(AnnouncementItem::getId)
                .eq(AnnouncementItem::getSessionId, sessionId)
        ).stream().map(AnnouncementItem::getId).toList();
        long ttlToEnd = Duration.between(now, bidEndDateTime).toMillis();

        /**
         * 确保事务成功提交后，再刷新缓存，防止mysql事务回滚，但Redis和MQ已执行
         * 数据库事务提交需要时间的，如果不加这个控制，Mysql还没执行完，Redis此时已经执行成功
         * 加上 afterCommit，数据库修改完状态，立即提交事务并释放行锁。主线程再慢悠悠的改Redis
         */
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Long itemId : itemIds) {
                    redisTemplate.opsForHash().put("bid:item:live:" + itemId, "item_status", "1");
                }
                rabbitTemplate.convertAndSend(BidMqConstants.EXCHANGE_CLOCK, BidMqConstants.KEY_BID_END_DELAY, msgPayload, message -> {
                    message.getMessageProperties().setDelayLong(ttlToEnd);
                    return message;
                });
                log.info("场次 {} 属于[过期紧急开拍]，Redis 已解锁，{} 毫秒后自动截拍", sessionId, ttlToEnd);
            }
        });
    }
}
