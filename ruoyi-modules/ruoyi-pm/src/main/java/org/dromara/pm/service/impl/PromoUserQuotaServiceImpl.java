package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.service.PromoUserQuotaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 活动级参与次数：Hash field=userId，value=已参与次数；每单每个活动最多 +1。无占痕。
 */
@Slf4j
@Service
public class PromoUserQuotaServiceImpl implements PromoUserQuotaService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> promoJoinReserveScript;

    public PromoUserQuotaServiceImpl(StringRedisTemplate stringRedisTemplate,
                                     @Qualifier("promoJoinReserveScript") DefaultRedisScript<Long> promoJoinReserveScript) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.promoJoinReserveScript = promoJoinReserveScript;
    }

    @Override
    public boolean hasRemainingJoinQuota(String tenant, String channel, Long userId, PromoActivity activity) {
        if (activity == null) {
            return false;
        }
        Long userLimitQty = activity.getUserLimitQty();
        if (userLimitQty == null || userLimitQty <= 0) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        long used = readUsedCount(tenant, channel, activity.getId(), userId);
        return used < userLimitQty;
    }

    /**
     * 给普通促销做 「活动参与次数」占次：每个活动、每张订单最多 +1（不是按买几件）。
     */
    @Override
    public List<Long> reserveJoinQuota(String tenant, String channel, Long userId, List<Long> activityIds,
                                       String parentOrderNo) {
        if (userId == null || CollUtil.isEmpty(activityIds) || StringUtils.isBlank(tenant) || StringUtils.isBlank(channel)) {
            return Collections.emptyList();
        }
        List<Long> reserved = new ArrayList<>();
        try {
            for (Long activityId : activityIds) {
                PromoActivity activity = loadActivity(tenant, channel, activityId);
                if (activity == null) {
                    continue;
                }
                Long userLimitQty = activity.getUserLimitQty();
                if (userLimitQty == null || userLimitQty <= 0) {
                    continue;
                }
                String key = PmCacheKeys.promoUserLimit(tenant, channel, activityId);
                long result = stringRedisTemplate.execute(
                    promoJoinReserveScript,
                    Collections.singletonList(key),
                    String.valueOf(userId),
                    String.valueOf(userLimitQty));
                if (result == -1L) {
                    throw new ServiceException("您已达到促销活动的参与次数上限，请返回重新结算");
                }
                if (result == 1L) {
                    reserved.add(activityId);
                }
            }
            return reserved;
        } catch (RuntimeException ex) {
            if (CollUtil.isNotEmpty(reserved)) {
                rollbackJoinQuota(tenant, channel, userId, reserved);
            }
            throw ex;
        }
    }

    /**
     * 把本单占用的活动参与次数退回 Redis。每个活动对该用户 -1。
     */
    @Override
    public void rollbackJoinQuota(String tenant, String channel, Long userId, List<Long> activityIds) {
        for (Long activityId : activityIds) {
            String key = PmCacheKeys.promoUserLimit(tenant, channel, activityId);
            // 该活动的用户参与次数
            Object raw = stringRedisTemplate.opsForHash().get(key, String.valueOf(userId));
            if (raw == null) {
                continue;
            }
            long used = Long.parseLong(raw.toString());
            long next = used - 1;
            if (next <= 0) {
                stringRedisTemplate.opsForHash().delete(key, String.valueOf(userId));
            } else {
                stringRedisTemplate.opsForHash().put(key, String.valueOf(userId), String.valueOf(next));
            }
        }
    }

    private long readUsedCount(String tenant, String channel, Long activityId, Long userId) {
        if (activityId == null || userId == null) {
            return 0L;
        }
        Object raw = stringRedisTemplate.opsForHash().get(
            PmCacheKeys.promoUserLimit(tenant, channel, activityId), String.valueOf(userId));
        if (raw == null) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private PromoActivity loadActivity(String tenant, String channel, Long activityId) {
        String json = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoDetail(tenant, channel, activityId));
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JsonUtils.parseObject(json, PromoActivity.class);
    }
}
