package org.dromara.dis.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.dis.domain.DistCommissionItem;
import org.dromara.dis.domain.DistCommissionOrder;
import org.dromara.dis.domain.DistLevel;
import org.dromara.dis.domain.DistRelation;
import org.dromara.dis.domain.DistSetting;
import org.dromara.dis.enums.DistCommissionStatus;
import org.dromara.dis.mapper.DistCommissionItemMapper;
import org.dromara.dis.mapper.DistCommissionOrderMapper;
import org.dromara.dis.mapper.DistLevelMapper;
import org.dromara.dis.mapper.DistRelationMapper;
import org.dromara.dis.mapper.DistSettingMapper;
import org.dromara.dis.service.IDistCommissionCalcService;
import org.dromara.order.api.mq.OrderPaidEvent;
import org.dromara.user.api.RemoteUserBizService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 支付成功 → 写分佣待结算
 * <p>比例：受益人有等级用等级比例，否则用 dist_setting 基础比例；商品级分佣后续再接。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistCommissionCalcServiceImpl implements IDistCommissionCalcService {

    private static final String IDENTITY_DIS = "dis";
    private static final long QUALIFY_ONLY_DIS = 1L;
    private static final int MAX_LEVEL = 3;

    private final DistCommissionOrderMapper commissionOrderMapper;
    private final DistCommissionItemMapper commissionItemMapper;
    private final DistRelationMapper distRelationMapper;
    private final DistSettingMapper distSettingMapper;
    private final DistLevelMapper distLevelMapper;

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPendingOnPaid(OrderPaidEvent event) {
        if (event == null
            || StringUtils.isBlank(event.getOrderNo())
            || event.getUserId() == null
            || event.getPaidAmount() == null
            || event.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("ORDER_PAID 分佣参数不完整，跳过 event={}", event);
            return;
        }

        Long exists = commissionOrderMapper.selectCount(
            Wrappers.<DistCommissionOrder>lambdaQuery()
                .eq(DistCommissionOrder::getOrderNo, event.getOrderNo())
        );
        if (exists != null && exists > 0) {
            log.info("ORDER_PAID 分佣已存在，幂等跳过 orderNo={}", event.getOrderNo());
            return;
        }

        DistSetting setting = distSettingMapper.selectOne(
            Wrappers.<DistSetting>lambdaQuery().last("LIMIT 1")
        );
        if (setting == null) {
            log.info("ORDER_PAID 无分销配置，跳过 orderNo={}", event.getOrderNo());
            return;
        }

        Long buyerId = event.getUserId();
        // 分销员自购无佣：整单不计佣
        if (Objects.equals(0L, setting.getIsBuyerHasCommission())
            && remoteUserBizService.hasIdentity(buyerId, IDENTITY_DIS)) {
            log.info("ORDER_PAID 分销员自购不计佣 orderNo={} buyerId={}", event.getOrderNo(), buyerId);
            return;
        }

        List<Long> upline = resolveUpline(buyerId, MAX_LEVEL);
        if (upline.isEmpty()) {
            log.info("ORDER_PAID 无上级，跳过分佣 orderNo={} buyerId={}", event.getOrderNo(), buyerId);
            return;
        }

        Long qualifyType = setting.getQualifyType() == null ? QUALIFY_ONLY_DIS : setting.getQualifyType();
        boolean onlyDis = Objects.equals(QUALIFY_ONLY_DIS, qualifyType);
        List<DistCommissionItem> items = new ArrayList<>();
        for (int i = 0; i < upline.size(); i++) {
            int level = i + 1;
            Long beneficiaryId = upline.get(i);
            if (onlyDis && !remoteUserBizService.hasIdentity(beneficiaryId, IDENTITY_DIS)) {
                continue;
            }
            BigDecimal rate = resolveRate(level, beneficiaryId, setting);
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal amount = event.getPaidAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            DistCommissionItem item = new DistCommissionItem();
            item.setOrderNo(event.getOrderNo());
            item.setBeneficiaryUserinfoId(beneficiaryId);
            item.setLevel(level);
            item.setRate(rate);
            item.setAmount(amount);
            item.setStatus(DistCommissionStatus.PENDING.getCode());
            items.add(item);
        }
        if (items.isEmpty()) {
            log.info("ORDER_PAID 无有效分佣明细 orderNo={}", event.getOrderNo());
            return;
        }

        DistCommissionOrder order = new DistCommissionOrder();
        order.setChannelCode(StringUtils.blankToDefault(event.getChannelCode(), "default"));
        order.setCommissionNo(IdGeneratorUtil.nextIdWithPrefix("DC"));
        order.setOrderNo(event.getOrderNo());
        order.setBuyerUserinfoId(buyerId);
        order.setOrderAmount(event.getPaidAmount());
        commissionOrderMapper.insert(order);

        for (DistCommissionItem item : items) {
            item.setCommissionOrderId(order.getId());
            item.setCommissionNo(order.getCommissionNo());
            commissionItemMapper.insert(item);
        }
        log.info("ORDER_PAID 分佣待结算已生成 orderNo={} commissionNo={} itemCount={}",
            event.getOrderNo(), order.getCommissionNo(), items.size());
    }

    /** 从买家向上找最多 maxLevel 级上级 */
    private List<Long> resolveUpline(Long buyerUserinfoId, int maxLevel) {
        List<Long> result = new ArrayList<>(maxLevel);
        Long current = buyerUserinfoId;
        for (int i = 0; i < maxLevel; i++) {
            DistRelation rel = distRelationMapper.selectOne(
                Wrappers.<DistRelation>lambdaQuery()
                    .eq(DistRelation::getUserinfoId, current)
                    .last("LIMIT 1")
            );
            if (rel == null || rel.getParentUserinfoId() == null) {
                break;
            }
            Long parentId = rel.getParentUserinfoId();
            if (parentId.equals(buyerUserinfoId) || result.contains(parentId)) {
                break;
            }
            result.add(parentId);
            current = parentId;
        }
        return result;
    }

    private BigDecimal resolveRate(int level, Long beneficiaryId, DistSetting setting) {
        Long levelId = remoteUserBizService.getDistLevelId(beneficiaryId);
        if (levelId != null) {
            DistLevel distLevel = distLevelMapper.selectById(levelId);
            if (distLevel != null) {
                BigDecimal rate = pickLevelRate(level, distLevel);
                if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                    return rate;
                }
            }
        }
        return pickSettingRate(level, setting);
    }

    private BigDecimal pickLevelRate(int level, DistLevel distLevel) {
        return switch (level) {
            case 1 -> distLevel.getFirstRate();
            case 2 -> distLevel.getSecondRate();
            case 3 -> distLevel.getThirdRate();
            default -> null;
        };
    }

    private BigDecimal pickSettingRate(int level, DistSetting setting) {
        return switch (level) {
            case 1 -> setting.getBaseFirstRate();
            case 2 -> setting.getBaseSecondRate();
            case 3 -> setting.getBaseThirdRate();
            default -> null;
        };
    }
}
