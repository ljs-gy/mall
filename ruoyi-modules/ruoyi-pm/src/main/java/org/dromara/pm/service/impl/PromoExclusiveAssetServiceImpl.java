package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.order.api.mq.SkuRollbackItem;
import org.dromara.pm.api.domain.RemoteExclusivePromoDeductBo;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.service.PromoExclusiveAssetService;
import org.dromara.pm.utils.MoneyFenUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class PromoExclusiveAssetServiceImpl implements PromoExclusiveAssetService {

    private static final String TYPE_SECKILL = "SECKILL";
    private static final String TYPE_SPECIAL = "SPECIAL_PRICE";
    private static final String TYPE_CROSS_FULL_REDUCE = "CROSS_FULL_REDUCE";
    private static final String TYPE_CROSS_FULL_DISCOUNT = "CROSS_FULL_DISCOUNT";

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> seckillDecrScript;
    private final DefaultRedisScript<Long> budgetDecrScript;

    public PromoExclusiveAssetServiceImpl(StringRedisTemplate stringRedisTemplate,
                                          @Qualifier("seckillDecrScript") DefaultRedisScript<Long> seckillDecrScript,
                                          @Qualifier("budgetDecrScript") DefaultRedisScript<Long> budgetDecrScript) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillDecrScript = seckillDecrScript;
        this.budgetDecrScript = budgetDecrScript;
    }

    @Override
    public void deductExclusivePromoStock(RemoteExclusivePromoDeductBo bo) {
        // 校验参数、活动存在，并判断是秒杀还是特价
        boolean isSeckill = validateAndCheckSeckill(bo);
        try {
            deductExclusiveRedis(bo, isSeckill);
        } catch (RuntimeException e) {
            throw e instanceof ServiceException
                ? (ServiceException) e
                : new ServiceException("活动库存占用失败，请稍后重试");
        }
    }

    /**
     * 按扣减时的入参，把专享 Redis 库存/限购加回去
     */
    @Override
    public void restoreExclusivePromoStock(RemoteExclusivePromoDeductBo bo) {
        if (bo == null || bo.getActivityId() == null || bo.getSkuId() == null || bo.getBuyCount() == null || bo.getBuyCount() <= 0) {
            return;
        }
        // 再确认是秒杀还是特价
        boolean isSeckill = resolveIsSeckill(bo.getTenantId(), bo.getChannelCode(), bo.getActivityId());
        restoreExclusiveSku(bo.getTenantId(), bo.getChannelCode(), bo.getActivityId(), isSeckill,
            bo.getUserId(), bo.getSkuId(), bo.getBuyCount());
        log.warn("专享 Redis 已按入参加回 tenant={} activityId={} skuId={} qty={}",
            bo.getTenantId(), bo.getActivityId(), bo.getSkuId(), bo.getBuyCount());
    }

    /**
     * 扣 跨店满减/满折的平台补贴预算，本单跨店优惠占掉多少预算就减多少。
     */
    @Override
    public boolean deductCrossPromoBudget(String tenantId, String channelCode, Long activityId,
                                          BigDecimal discountAmount, String parentOrderNo) {
        if (activityId == null || discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        String actJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoDetail(tenantId, channelCode, activityId));
        if (StringUtils.isBlank(actJson)) {
            return false;
        }
        PromoActivity act = JsonUtils.parseObject(actJson, PromoActivity.class);
        if (act == null) {
            return false;
        }
        String promoType = act.getPromoType();
        if (!TYPE_CROSS_FULL_REDUCE.equalsIgnoreCase(promoType)
            && !TYPE_CROSS_FULL_DISCOUNT.equalsIgnoreCase(promoType)) {
            return false;
        }
        String budgetKey = PmCacheKeys.promoBudget(tenantId, channelCode, activityId);
        long decrFen = MoneyFenUtils.toFen(discountAmount);
        Long res = stringRedisTemplate.execute(budgetDecrScript,
            Collections.singletonList(budgetKey), String.valueOf(decrFen));
        return res != 0L;
    }

    /**
     * 把本单占用的跨店优惠金额加回 Redis 预算池。
     */
    @Override
    public void restoreCrossPromoBudget(String tenantId, String channelCode, Long activityId, BigDecimal discountAmount) {
        String budgetKey = PmCacheKeys.promoBudget(tenantId, channelCode, activityId);
        long fen = MoneyFenUtils.toFen(discountAmount);
        String raw = stringRedisTemplate.opsForValue().get(budgetKey);
        if (StringUtils.isBlank(raw)) {
            return;
        }
        stringRedisTemplate.opsForValue().increment(budgetKey, fen);
        log.info("跨店预算已加回 actId={} fen={}", activityId, fen);
    }


    @Override
    public boolean tryMarkAssetRefunded(String tenantId, String channelCode, String parentOrderNo, String assetType) {
        String key = PmCacheKeys.orderPromoAssetRefund(tenantId, channelCode, parentOrderNo, assetType);
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", PmCacheKeys.ORDER_REFUND_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }

    /**
     * 回滚失败时删 mark，允许重试。
     */
    @Override
    public void clearAssetRefundedMark(String tenantId, String channelCode, String parentOrderNo, String assetType) {
        if (StringUtils.isBlank(parentOrderNo) || StringUtils.isBlank(assetType)) {
            return;
        }
        stringRedisTemplate.delete(
            PmCacheKeys.orderPromoAssetRefund(tenantId, channelCode, parentOrderNo, assetType));
    }

    /**
     * 按 SKU 清单，把专享活动秒杀/特价占用的 Redis 资产加回去。
     */
    @Override
    public void restoreExclusiveByItems(String tenantId, String channelCode, Long activityId, boolean isSeckill, Long userId, List<SkuRollbackItem> items) {
        for (SkuRollbackItem item : items) {
            restoreExclusiveSku(tenantId, channelCode, activityId, isSeckill, userId, item.getSkuId(), item.getBuyCount());
        }
    }

    private void deductExclusiveRedis(RemoteExclusivePromoDeductBo bo, boolean isSeckill) {
        String tenant = bo.getTenantId();
        String channel = bo.getChannelCode();
        Long activityId = bo.getActivityId();
        Long skuId = bo.getSkuId();
        int buyCount = bo.getBuyCount();
        Long userId = bo.getUserId();
        String skuCfgKey = isSeckill
            ? PmCacheKeys.seckillSku(tenant, channel, activityId, skuId)
            : PmCacheKeys.specialSku(tenant, channel, activityId, skuId);
        String cfgJson = stringRedisTemplate.opsForValue().get(skuCfgKey);
        long userLimit = -1L;
        if (StringUtils.isNotBlank(cfgJson)) {
            PromoGoodsSku cfg = JsonUtils.parseObject(cfgJson, PromoGoodsSku.class);
            if (cfg != null && cfg.getUserLimitQty() != null) {
                userLimit = cfg.getUserLimitQty();
            }
        }
        String stockKey = isSeckill
            ? PmCacheKeys.seckillStock(tenant, channel, activityId, skuId)
            : PmCacheKeys.specialStock(tenant, channel, activityId, skuId);
        String limitKey = isSeckill
            ? PmCacheKeys.seckillUserLimit(tenant, channel, activityId, skuId)
            : PmCacheKeys.specialUserLimit(tenant, channel, activityId, skuId);
        Long result = stringRedisTemplate.execute(
            seckillDecrScript,
            java.util.Arrays.asList(stockKey, limitKey),
            String.valueOf(userId),
            String.valueOf(buyCount),
            String.valueOf(userLimit)
        );
        if (result == -1L) {
            throw new ServiceException("商品(SKU:" + skuId + ")已超出单人活动限购上限");
        }
        if (result == -2L) {
            throw new ServiceException("手慢了！商品(SKU:" + skuId + ")活动专享库存不足");
        }
    }

    /**
     * 加回活动库存，并扣减该用户的已购数量
     */
    private void restoreExclusiveSku(String tenant, String channel, Long activityId, boolean isSeckill, Long userId, Long skuId, int buyCount) {
        String stockKey = isSeckill
            ? PmCacheKeys.seckillStock(tenant, channel, activityId, skuId)
            : PmCacheKeys.specialStock(tenant, channel, activityId, skuId);
        String limitKey = isSeckill
            ? PmCacheKeys.seckillUserLimit(tenant, channel, activityId, skuId)
            : PmCacheKeys.specialUserLimit(tenant, channel, activityId, skuId);
        String rawStock = stringRedisTemplate.opsForValue().get(stockKey);
        if (StringUtils.isNotBlank(rawStock)) {
            long stock = Long.parseLong(rawStock);
            if (stock >= 0) {
                // 把本单占用的数量加回活动库存
                stringRedisTemplate.opsForValue().increment(stockKey, buyCount);
            }
        }
        Object raw = stringRedisTemplate.opsForHash().get(limitKey, String.valueOf(userId));
        if (raw != null) {
            // 该用户在这活动 SKU 上已占数量
            long used = Long.parseLong(raw.toString());
            // 需要扣掉的数量 = 已占数量 - 本单占用的数量
            long next = used - buyCount;
            if (next <= 0) {
                stringRedisTemplate.opsForHash().delete(limitKey, String.valueOf(userId));
            } else {
                stringRedisTemplate.opsForHash().put(limitKey, String.valueOf(userId), String.valueOf(next));
            }
        }
    }

    private boolean validateAndCheckSeckill(RemoteExclusivePromoDeductBo bo) {
        if (bo == null || bo.getActivityId() == null
            || bo.getSkuId() == null || bo.getBuyCount() == null || bo.getBuyCount() <= 0) {
            throw new ServiceException("专享促销扣减参数不完整");
        }
        return resolveIsSeckill(bo.getTenantId(), bo.getChannelCode(), bo.getActivityId());
    }

    private boolean resolveIsSeckill(String tenant, String channel, Long activityId) {
        String actJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.promoDetail(tenant, channel, activityId));
        if (StringUtils.isBlank(actJson)) {
            throw new ServiceException("促销活动不存在或已结束");
        }
        PromoActivity activity = JsonUtils.parseObject(actJson, PromoActivity.class);
        if (activity == null) {
            throw new ServiceException("促销活动不存在或已结束");
        }
        boolean isSeckill = TYPE_SECKILL.equalsIgnoreCase(activity.getPromoType());
        boolean isSpecial = TYPE_SPECIAL.equalsIgnoreCase(activity.getPromoType());
        if (!isSeckill && !isSpecial) {
            throw new ServiceException("指定活动非秒杀或特价活动");
        }
        return isSeckill;
    }
}
