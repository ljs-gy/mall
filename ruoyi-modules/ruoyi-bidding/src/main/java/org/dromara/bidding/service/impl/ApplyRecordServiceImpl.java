package org.dromara.bidding.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.bidding.domain.ApplyItemRel;
import org.dromara.bidding.domain.ApplyRecord;
import org.dromara.bidding.domain.Session;
import org.dromara.bidding.domain.UserDepositLog;
import org.dromara.bidding.domain.bo.ApplyRecordBo;
import org.dromara.bidding.domain.bo.AuditBuyerApplyBo;
import org.dromara.bidding.domain.bo.SubmitBidApplyBo;
import org.dromara.bidding.domain.vo.ApplyRecordVo;
import org.dromara.bidding.enums.ApplyAuditStatus;
import org.dromara.bidding.enums.DepositDirection;
import org.dromara.bidding.enums.SessionBidStatus;
import org.dromara.bidding.mapper.AnnouncementItemMapper;
import org.dromara.bidding.mapper.ApplyItemRelMapper;
import org.dromara.bidding.mapper.ApplyRecordMapper;
import org.dromara.bidding.mapper.SessionMapper;
import org.dromara.bidding.mapper.UserDepositLogMapper;
import org.dromara.bidding.service.IApplyRecordService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.redis.utils.SequenceUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 报名记录Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ApplyRecordServiceImpl implements IApplyRecordService {

    private static final int DEPOSIT_TYPE_SESSION = 1;
    private static final int DEPOSIT_TYPE_ITEM = 2;

    private final ApplyRecordMapper baseMapper;
    private final ApplyItemRelMapper applyItemRelMapper;
    private final SessionMapper sessionMapper;
    private final AnnouncementItemMapper announcementItemMapper;
    private final UserDepositLogMapper userDepositLogMapper;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;
    private final StringRedisTemplate redisTemplate;

    @Override
    public ApplyRecordVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ApplyRecordVo> queryPageList(ApplyRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ApplyRecord> lqw = buildQueryWrapper(bo);
        Page<ApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<ApplyRecordVo> queryList(ApplyRecordBo bo) {
        LambdaQueryWrapper<ApplyRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ApplyRecord> buildQueryWrapper(ApplyRecordBo bo) {
        LambdaQueryWrapper<ApplyRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getApplyNo()), ApplyRecord::getApplyNo, bo.getApplyNo());
        lqw.eq(bo.getSessionId() != null, ApplyRecord::getSessionId, bo.getSessionId());
        lqw.eq(bo.getUserId() != null, ApplyRecord::getUserId, bo.getUserId());
        lqw.eq(bo.getAuditStatus() != null, ApplyRecord::getAuditStatus, bo.getAuditStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(ApplyRecordBo bo) {
        ApplyRecord add = MapstructUtils.convert(bo, ApplyRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(ApplyRecordBo bo) {
        ApplyRecord update = MapstructUtils.convert(bo, ApplyRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(ApplyRecord entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public void submitBidApply(SubmitBidApplyBo bo) {
        Long userId = LoginHelper.getUserId();
        String tenantId = LoginHelper.getTenantId();
        Long sessionId = bo.getSessionId();
        List<Long> itemIds = bo.getItemIds();

        String lockKey = "lock:bid:submit:" + tenantId + ":" + sessionId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(2, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new ServiceException("您的申请正在处理中，请勿重复点击提交！");
            }

            transactionTemplate.execute(status -> {
                doSubmitBidApply(bo, sessionId, itemIds, userId, tenantId);
                return true;
            });
        } catch (InterruptedException e) {
            log.error("分布式锁获取遭遇中断异常, lockKey: {}", lockKey, e);
            Thread.currentThread().interrupt();
            throw new ServiceException("服务器拥挤，请稍后再试！");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void doSubmitBidApply(SubmitBidApplyBo bo, Long sessionId, List<Long> itemIds,
                                  Long userId, String tenantId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException("报名失败：对应的竞价场次不存在！");
        }
        if (session.getApplyDeadline() != null) {
            LocalDateTime deadline = DateUtil.toLocalDateTime(session.getApplyDeadline());
            if (LocalDateTime.now().isAfter(deadline)) {
                throw new ServiceException("报名拦截：已超过该场次规定的交保报名截止死线！");
            }
        }
        if (session.getBidStatus() != null && session.getBidStatus() >= SessionBidStatus.ENDED.getCode()) {
            throw new ServiceException("报名拦截：该场次竞价已到期截拍或已完结，拒绝报名！");
        }

        int depositType = session.getDepositType();
        List<AnnouncementItem> validItems = null;
        if (CollUtil.isNotEmpty(itemIds)) {
            validItems = announcementItemMapper.selectBatchIds(itemIds);
            for (AnnouncementItem item : validItems) {
                if (!sessionId.equals(item.getSessionId())) {
                    throw new ServiceException("安全拦截：检测到非法操作，勾选的商品不属于当前场次活动！");
                }
            }
        }

        if (depositType == DEPOSIT_TYPE_SESSION) {
            int activeCount = baseMapper.countActiveApply(userId, sessionId, tenantId);
            if (activeCount > 0) {
                throw new ServiceException("合规拦截：本场为通票模式，您已有处理中或已通过的报名申请，请勿重复提交！");
            }
        } else if (depositType == DEPOSIT_TYPE_ITEM) {
            if (CollUtil.isEmpty(itemIds)) {
                throw new ServiceException("合规拦截：本场属于【按商品独立交保】，请至少选择一个意向标的货物！");
            }
            int duplicateItemCount = applyItemRelMapper.countDuplicateItems(userId, sessionId, tenantId, itemIds);
            if (duplicateItemCount > 0) {
                throw new ServiceException("合规拦截：您勾选的商品中包含了已经成功交保或正在审核中的货物，请检查后重新勾选！");
            }
        } else {
            throw new ServiceException("系统暂未支持此类型的保证金收缴策略！");
        }

        BigDecimal totalNeedDeposit = BigDecimal.ZERO;
        if (depositType == DEPOSIT_TYPE_SESSION) {
            totalNeedDeposit = session.getFullDepositAmount();
        } else {
            for (AnnouncementItem item : validItems) {
                totalNeedDeposit = totalNeedDeposit.add(item.getItemDepositAmount());
            }
        }

        ApplyRecord newRecord = new ApplyRecord();
        newRecord.setTenantId(tenantId);
        newRecord.setApplyNo(SequenceUtils.getDateTimeId("AP", true));
        newRecord.setSessionId(sessionId);
        newRecord.setUserId(userId);
        newRecord.setNeedDepositAmount(totalNeedDeposit);
        newRecord.setRefundableDeposit(BigDecimal.ZERO);
        newRecord.setAuditingDeposit(BigDecimal.ZERO);
        newRecord.setPayProofImage(bo.getPayProofImage());
        newRecord.setPayRemark(bo.getPayRemark());
        newRecord.setQualificationProofs(bo.getQualificationProofs());
        newRecord.setAuditStatus(ApplyAuditStatus.PENDING.getCode());
        baseMapper.insert(newRecord);

        if (depositType == DEPOSIT_TYPE_ITEM && CollUtil.isNotEmpty(validItems)) {
            insertItemRelations(newRecord.getId(), validItems);
        }

        log.info("买家 {} 成功提交报名子单 {}，本单应交保证金 {} 元，商品滚动补报校验通过。",
            userId, newRecord.getApplyNo(), totalNeedDeposit);
    }

    private void insertItemRelations(Long applyId, List<AnnouncementItem> validItems) {
        for (AnnouncementItem item : validItems) {
            ApplyItemRel rel = new ApplyItemRel();
            rel.setApplyId(applyId);
            rel.setItemId(item.getId());
            rel.setDepositAmount(item.getItemDepositAmount());
            applyItemRelMapper.insert(rel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBuyerApply(AuditBuyerApplyBo bo) {
        Long applyId = bo.getApplyId();
        Integer auditStatus = bo.getAuditStatus();
        String operatorUser = LoginHelper.getUsername();

        ApplyRecord record = baseMapper.selectForUpdate(applyId);
        if (record == null) {
            throw new ServiceException("未找到该笔报名申请单！");
        }
        if (record.getAuditStatus() != ApplyAuditStatus.PENDING.getCode()) {
            throw new ServiceException("审批异常：该单据已被处理，请勿重复操作！");
        }

        record.setAuditStatus(auditStatus);
        record.setAuditUser(operatorUser);
        record.setAuditOpinion(bo.getAuditOpinion());

        if (auditStatus == ApplyAuditStatus.APPROVED.getCode()) {
            record.setAuditingDeposit(record.getNeedDepositAmount());
            baseMapper.updateById(record);

            UserDepositLog depositLog = new UserDepositLog();
            depositLog.setTenantId(record.getTenantId());
            depositLog.setLogCode(SequenceUtils.getDateTimeId("LOG", true));
            depositLog.setUserId(record.getUserId());
            depositLog.setSessionId(record.getSessionId());
            depositLog.setAmount(record.getNeedDepositAmount());
            depositLog.setDirection(DepositDirection.APPLY_FREEZE.getCode());
            depositLog.setBeforeRefundableDeposit(BigDecimal.ZERO);
            depositLog.setAfterRefundableDeposit(BigDecimal.ZERO);
            depositLog.setBeforeAuditingDeposit(BigDecimal.ZERO);
            depositLog.setAfterAuditingDeposit(record.getNeedDepositAmount());
            depositLog.setBizNo(record.getApplyNo());
            depositLog.setRemark("运营人工审核线下电汇单据通过，系统正式完成行级资金账面冷冻");
            depositLog.setOperatorUser(operatorUser);
            userDepositLogMapper.insert(depositLog);

            Session session = sessionMapper.selectById(record.getSessionId());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime bidEndDateTime = DateUtil.toLocalDateTime(session.getEndTime());
            long expireMillis = Duration.between(now, bidEndDateTime).toMillis() + TimeUnit.DAYS.toMillis(7);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    activateWhitelist(record, session, expireMillis);
                }
            });

            log.info("运营 {} 审批通过单据 {}，买家 {} 保证金正式确权冷冻，白名单将在事务提交后激活！",
                operatorUser, record.getApplyNo(), record.getUserId());
        } else if (auditStatus == ApplyAuditStatus.REJECTED.getCode()) {
            baseMapper.updateById(record);
            log.warn("运营 {} 驳回了买家 {} 的交保申请，驳回原因: {}", operatorUser, record.getUserId(), bo.getAuditOpinion());
        } else {
            throw new ServiceException("非法的审批动作状态指令！");
        }
    }

    private void activateWhitelist(ApplyRecord record, Session session, long expireMillis) {
        if (session.getDepositType() == DEPOSIT_TYPE_SESSION) {
            String sessionWhitelistKey = "bid:whitelist:session:" + record.getSessionId();
            redisTemplate.opsForSet().add(sessionWhitelistKey, record.getUserId().toString());
            redisTemplate.expire(sessionWhitelistKey, expireMillis, TimeUnit.MILLISECONDS);
        } else {
            List<Long> allowedItemIds = applyItemRelMapper.selectItemIdsByApplyId(record.getId());
            for (Long itemId : allowedItemIds) {
                String itemWhitelistKey = "bid:whitelist:item:" + itemId;
                redisTemplate.opsForSet().add(itemWhitelistKey, record.getUserId().toString());
                redisTemplate.expire(itemWhitelistKey, expireMillis, TimeUnit.MILLISECONDS);
            }
        }
    }
}
