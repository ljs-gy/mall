package org.dromara.bidding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.bidding.domain.BidLog;
import org.dromara.bidding.domain.bo.BidLogBo;
import org.dromara.bidding.domain.bo.DoBidBo;
import org.dromara.bidding.domain.vo.BidLogVo;
import org.dromara.bidding.mapper.AnnouncementItemMapper;
import org.dromara.bidding.mapper.BidLogMapper;
import org.dromara.bidding.mq.BidMqConstants;
import org.dromara.bidding.service.IBidLogService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.redis.utils.SequenceUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出价流水Service业务层处理
 *
 * @author ljs
 * @date 2026-06-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BidLogServiceImpl implements IBidLogService {

    private static final DateTimeFormatter BID_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BidLogMapper baseMapper;
    private final AnnouncementItemMapper announcementItemMapper;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private DefaultRedisScript<Long> bidScript;

    @PostConstruct
    public void initBidScript() {
        bidScript = new DefaultRedisScript<>();
        bidScript.setResultType(Long.class);
        bidScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("bid.lua")));
    }

    @Override
    public BidLogVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BidLogVo> queryPageList(BidLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BidLog> lqw = buildQueryWrapper(bo);
        Page<BidLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BidLogVo> queryList(BidLogBo bo) {
        LambdaQueryWrapper<BidLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BidLog> buildQueryWrapper(BidLogBo bo) {
        LambdaQueryWrapper<BidLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getBidLogCode()), BidLog::getBidLogCode, bo.getBidLogCode());
        lqw.eq(bo.getSessionId() != null, BidLog::getSessionId, bo.getSessionId());
        lqw.eq(bo.getItemId() != null, BidLog::getItemId, bo.getItemId());
        lqw.eq(bo.getUserId() != null, BidLog::getUserId, bo.getUserId());
        lqw.eq(bo.getBidStatus() != null, BidLog::getBidStatus, bo.getBidStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(BidLogBo bo) {
        BidLog add = MapstructUtils.convert(bo, BidLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(BidLogBo bo) {
        BidLog update = MapstructUtils.convert(bo, BidLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(BidLog entity) {
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
    @Transactional(rollbackFor = Exception.class)
    public void doBidAdvanced(DoBidBo bo) {
        Long sessionId = bo.getSessionId();
        Long itemId = bo.getItemId();
        Long userId = LoginHelper.getUserId();
        String tenantId = LoginHelper.getTenantId();
        BigDecimal bidPrice = bo.getBidPrice();

        String itemLiveKey = "bid:item:live:" + itemId;
        String sessionHashKey = "bid:session:info:" + sessionId;
        String sessionWhitelistKey = "bid:whitelist:session:" + sessionId;
        String itemWhitelistKey = "bid:whitelist:item:" + itemId;

        Map<Object, Object> itemCache = redisTemplate.opsForHash().entries(itemLiveKey);
        Map<Object, Object> sessionCache = redisTemplate.opsForHash().entries(sessionHashKey);
        if (itemCache.isEmpty() || sessionCache.isEmpty()) {
            throw new ServiceException("出价失败：该竞价场次或商品包裹未在内存战场中成功预热！");
        }

        boolean isDelayEnabled = "1".equals(String.valueOf(sessionCache.get("is_delay_enabled")));
        LocalDateTime now = LocalDateTime.now();

        String itemEndTimeStr = stringVal(itemCache.get("item_end_time"));
        String sessionEndTimeStr = stringVal(sessionCache.get("end_time"));
        String fallbackEndTime = StringUtils.isNotBlank(itemEndTimeStr) ? itemEndTimeStr : sessionEndTimeStr;
        LocalDateTime currentItemEndTime = LocalDateTime.parse(fallbackEndTime, BID_TIME_FMT);
        if (now.isAfter(currentItemEndTime)) {
            throw new ServiceException("出价失败：该标的包裹已在时刻线截拍落槌！");
        }

        int isDelayedSignal = 0;
        String newEndTimeStr = "";
        long newTtl = 0;

        if (isDelayEnabled) {
            long secondsToLimit = Duration.between(now, currentItemEndTime).getSeconds();
            int delayTriggerMinutes = Integer.parseInt(stringVal(sessionCache.get("delay_trigger_minutes"), "5"));
            if (secondsToLimit >= 0 && secondsToLimit < (delayTriggerMinutes * 60L)) {
                int maxDelayMinutes = Integer.parseInt(stringVal(sessionCache.get("max_delay_minutes"), "60"));
                String originalEndTimeStr = stringVal(itemCache.get("original_end_time"));
                if (StringUtils.isNotBlank(originalEndTimeStr)) {
                    LocalDateTime originalEndTime = LocalDateTime.parse(originalEndTimeStr, BID_TIME_FMT);
                    LocalDateTime maxAllowedEndTime = originalEndTime.plusMinutes(maxDelayMinutes);
                    if (currentItemEndTime.isBefore(maxAllowedEndTime)) {
                        LocalDateTime newEndTime = now.plusMinutes(delayTriggerMinutes);
                        if (newEndTime.isAfter(maxAllowedEndTime)) {
                            newEndTime = maxAllowedEndTime;
                        }
                        isDelayedSignal = 1;
                        newEndTimeStr = newEndTime.format(BID_TIME_FMT);
                        newTtl = Duration.between(now, newEndTime).toMillis();
                    }
                }
            }
        }

        Long luaResult = redisTemplate.execute(
            bidScript,
            Arrays.asList(itemLiveKey, sessionWhitelistKey, itemWhitelistKey, sessionHashKey),
            userId.toString(),
            bidPrice.toPlainString(),
            String.valueOf(isDelayedSignal),
            newEndTimeStr
        );

        if (luaResult == null || luaResult < 0) {
            handleLuaError(luaResult);
        }

        BigDecimal basePrice = new BigDecimal(stringVal(itemCache.get("priceset_n_price"), "0"));
        BigDecimal quantity = new BigDecimal(stringVal(itemCache.get("goods_quantity"), "0"));
        BigDecimal bidPriceUnit = basePrice.add(bidPrice);
        BigDecimal bidTotalMoney = bidPriceUnit.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        if (luaResult == 2) {
            insertBidLog(tenantId, sessionId, itemId, userId, bidPrice, bidPriceUnit, bidTotalMoney, quantity, now, 2);
            log.info("【自由竞价低价沉淀成功】买家 {} 出价溢价 {} 元/吨属于影子低价，系统静默记流水。", userId, bidPrice);
            return;
        }

        if (luaResult == 10 || luaResult == 11) {
            Date finalNewEndTime = (luaResult == 11) ? Timestamp.valueOf(LocalDateTime.parse(newEndTimeStr, BID_TIME_FMT)) : null;
            int updateCount = announcementItemMapper.updateMaxBidProgress(
                itemId, bidPrice, bidPriceUnit, bidTotalMoney, userId, finalNewEndTime
            );
            if (updateCount == 0) {
                throw new ServiceException("出价失败：已被同秒更快的有效出价抢占，请刷新后重新出价！");
            }

            insertBidLog(tenantId, sessionId, itemId, userId, bidPrice, bidPriceUnit, bidTotalMoney, quantity, now, 1);

            if (luaResult == 11) {
                Map<String, Object> endPayload = new HashMap<>();
                endPayload.put("sessionId", sessionId);
                endPayload.put("itemId", itemId);
                endPayload.put("tenantId", tenantId);
                long finalNewTtl = newTtl;
                rabbitTemplate.convertAndSend(BidMqConstants.EXCHANGE_BUSINESS, BidMqConstants.KEY_ITEM_END_DELAY, endPayload, message -> {
                    message.getMessageProperties().setDelayLong(finalNewTtl);
                    return message;
                });
                log.info("【单单独立顺延成功】买家 {} 激活拖堂！包裹 {} 截止时间推迟到 {}。", userId, itemId, newEndTimeStr);
            }
        }
    }

    private void insertBidLog(String tenantId, Long sessionId, Long itemId, Long userId,
                              BigDecimal bidPrice, BigDecimal bidPriceUnit, BigDecimal bidTotalMoney,
                              BigDecimal quantity, LocalDateTime bidTime, int bidStatus) {
        BidLog bidLog = new BidLog();
        bidLog.setTenantId(tenantId);
        bidLog.setBidLogCode(SequenceUtils.getDateTimeId("BL", true));
        bidLog.setSessionId(sessionId);
        bidLog.setItemId(itemId);
        bidLog.setUserId(userId);
        bidLog.setBidPrice(bidPrice);
        bidLog.setBidPriceUnit(bidPriceUnit);
        bidLog.setBidTotalMoney(bidTotalMoney);
        bidLog.setGoodsQuantity(quantity);
        bidLog.setBidTime(Timestamp.valueOf(bidTime));
        bidLog.setBidStatus(bidStatus);
        baseMapper.insert(bidLog);
    }

    private void handleLuaError(Long result) {
        if (result == null || result == -1) {
            throw new ServiceException("出价失败：您未交纳足额保证金或未获得本场次准入资格！");
        }
        if (result == -2) {
            throw new ServiceException("出价失败：该标的包裹当前不在开放竞价时间段！");
        }
        if (result == -3) {
            throw new ServiceException("出价失败：非法出价金额，参数异常！");
        }
        if (result == -4) {
            throw new ServiceException("出价失败：您的出价已冲破该包裹配置的最高溢价天花板红线！");
        }
        if (result == -5) {
            throw new ServiceException("出价失败：对不起，该包裹已被系统判定截拍，无权改单！");
        }
        if (result == -6) {
            throw new ServiceException("出价失败：您的出价不能低于或等于当前全场最高溢价！");
        }
        if (result == -7) {
            throw new ServiceException("出价失败：明标加价不满足该商品配置的最小加价幅度步长！");
        }
        throw new ServiceException("出价服务器拥挤，请稍后再试！");
    }

    private static String stringVal(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String stringVal(Object value, String defaultValue) {
        String str = stringVal(value);
        return StringUtils.isNotBlank(str) ? str : defaultValue;
    }
}
