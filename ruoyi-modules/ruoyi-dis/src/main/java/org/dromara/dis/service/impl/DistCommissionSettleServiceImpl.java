package org.dromara.dis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.dis.domain.DistCommissionItem;
import org.dromara.dis.enums.DistCommissionStatus;
import org.dromara.dis.mapper.DistCommissionItemMapper;
import org.dromara.dis.service.IDistCommissionSettleService;
import org.dromara.order.api.mq.OrderCompletedEvent;
import org.dromara.user.api.RemoteUserBizService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 待结算 → 已结算并入佣金账户
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistCommissionSettleServiceImpl implements IDistCommissionSettleService {

    private final DistCommissionItemMapper commissionItemMapper;

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleOnCompleted(OrderCompletedEvent event) {
        if (event == null || StringUtils.isBlank(event.getOrderNo())) {
            log.warn("ORDER_COMPLETED 分佣结算参数不完整，跳过 event={}", event);
            return;
        }
        List<DistCommissionItem> items = commissionItemMapper.selectList(
            Wrappers.<DistCommissionItem>lambdaQuery()
                .eq(DistCommissionItem::getOrderNo, event.getOrderNo())
                .eq(DistCommissionItem::getStatus, DistCommissionStatus.PENDING.getCode())
        );
        if (items == null || items.isEmpty()) {
            log.info("ORDER_COMPLETED 无待结算分佣明细 orderNo={}", event.getOrderNo());
            return;
        }

        Date now = new Date();
        int settled = 0;
        for (DistCommissionItem item : items) {
            String bizOrderNo = "DCITEM:" + item.getId();
            String journalNo = remoteUserBizService.creditCommission(
                item.getBeneficiaryUserinfoId(), item.getAmount(), bizOrderNo);
            if (StringUtils.isBlank(journalNo)) {
                throw new IllegalStateException("佣金入账失败 itemId=" + item.getId());
            }
            item.setStatus(DistCommissionStatus.SETTLED.getCode());
            item.setSettleTime(now);
            item.setAccountJournalNo(journalNo);
            commissionItemMapper.updateById(item);
            settled++;
        }
        log.info("ORDER_COMPLETED 分佣入账完成 orderNo={} settled={}", event.getOrderNo(), settled);
    }
}
