package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.domain.PromoScopeTarget;
import org.dromara.pm.domain.PromoScopeUser;
import org.dromara.pm.domain.PromoTierGift;
import org.dromara.pm.domain.PromoTierRule;
import org.dromara.pm.domain.bo.PromoActivityAuditBo;
import org.dromara.pm.domain.bo.PromoActivityBo;
import org.dromara.pm.domain.bo.PromoGiftItemBo;
import org.dromara.pm.domain.bo.PromoTierRuleItemBo;
import org.dromara.pm.domain.bo.ExclusivePromoActivityBo;
import org.dromara.pm.domain.bo.ExclusiveSkuItem;
import org.dromara.pm.domain.vo.PromoActivityVo;
import org.dromara.pm.mapper.PromoActivityMapper;
import org.dromara.pm.mapper.PromoGoodsSkuMapper;
import org.dromara.pm.mapper.PromoScopeTargetMapper;
import org.dromara.pm.mapper.PromoScopeUserMapper;
import org.dromara.pm.mapper.PromoTierGiftMapper;
import org.dromara.pm.mapper.PromoTierRuleMapper;
import org.dromara.goods.api.mq.ExclusivePromoEsChangeEvent;
import org.dromara.goods.api.mq.GoodsExclusivePromoEsMqConstants;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.mq.CouponMqConstants;
import org.dromara.pm.service.IPromoActivityService;
import org.dromara.pm.utils.MoneyFenUtils;
import org.dromara.pm.utils.PromoScopeUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 营销活动主Service业务层处理
 *
 * @author ljs
 * @date 2026-07-21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PromoActivityServiceImpl implements IPromoActivityService {

    private static final long SOURCE_TYPE_PROMO = 1L;
    private static final String PROMO_TYPE_FULL_GIFT = "FULL_GIFT";
    private static final String PROMO_TYPE_FULL_REDUCE = "FULL_REDUCE";
    private static final String PROMO_TYPE_FULL_DISCOUNT = "FULL_DISCOUNT";
    private static final String PROMO_TYPE_SECKILL = "SECKILL";
    private static final String PROMO_TYPE_SPECIAL = "SPECIAL_PRICE";
    private static final String PROMO_TYPE_CROSS_REDUCE = "CROSS_FULL_REDUCE";
    private static final String PROMO_TYPE_CROSS_DISCOUNT = "CROSS_FULL_DISCOUNT";

    private final PromoActivityMapper baseMapper;
    private final PromoTierRuleMapper promoTierRuleMapper;
    private final PromoTierGiftMapper promoTierGiftMapper;
    private final PromoGoodsSkuMapper promoGoodsSkuMapper;
    private final PromoScopeTargetMapper promoScopeTargetMapper;
    private final PromoScopeUserMapper promoScopeUserMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public PromoActivityVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<PromoActivityVo> queryPageList(PromoActivityBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PromoActivity> lqw = buildQueryWrapper(bo);
        Page<PromoActivityVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<PromoActivityVo> queryList(PromoActivityBo bo) {
        LambdaQueryWrapper<PromoActivity> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PromoActivity> buildQueryWrapper(PromoActivityBo bo) {
        LambdaQueryWrapper<PromoActivity> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(PromoActivity::getCreateTime);
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), PromoActivity::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getPromoType()), PromoActivity::getPromoType, bo.getPromoType());
        lqw.eq(bo.getCalcType() != null, PromoActivity::getCalcType, bo.getCalcType());
        lqw.eq(bo.getScopeItemType() != null, PromoActivity::getScopeItemType, bo.getScopeItemType());
        lqw.eq(bo.getStatus() != null, PromoActivity::getStatus, bo.getStatus());
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(PromoActivityBo bo) {
        if (bo.getEndTime() != null && bo.getStartTime() != null && bo.getEndTime().before(bo.getStartTime())) {
            throw new ServiceException("活动结束时间不能早于开始时间");
        }
        boolean isFullGift = PROMO_TYPE_FULL_GIFT.equals(bo.getPromoType());
        boolean isDiscount = PROMO_TYPE_FULL_DISCOUNT.equals(bo.getPromoType()) || PROMO_TYPE_CROSS_DISCOUNT.equals(bo.getPromoType());
        boolean isReduce = PROMO_TYPE_FULL_REDUCE.equals(bo.getPromoType()) || PROMO_TYPE_CROSS_REDUCE.equals(bo.getPromoType());
        if (bo.getScopeItemType() == null) {
            throw new ServiceException("商品范围类型不能为空");
        }
        boolean isCross = PROMO_TYPE_CROSS_REDUCE.equals(bo.getPromoType()) || PROMO_TYPE_CROSS_DISCOUNT.equals(bo.getPromoType());
        if (isCross) {
            if (bo.getBudgetTotal() == null || bo.getBudgetTotal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("预算总额必须大于0");
            }
            if (bo.getPlatformShareRate() == null || bo.getMerchantShareRate() == null) {
                throw new ServiceException("平台/商家分摊比例不能为空");
            }
            if (bo.getUserScopeType() == null) {
                throw new ServiceException("用户人群范围不能为空");
            }
            if (PROMO_TYPE_CROSS_REDUCE.equals(bo.getPromoType()) && bo.getIsCap() == null) {
                throw new ServiceException("是否上不封顶不能为空");
            }
            if (PROMO_TYPE_CROSS_DISCOUNT.equals(bo.getPromoType())) {
                bo.setIsCap(0L);
            }
        }
        if (bo.getPlatformShareRate() != null && bo.getMerchantShareRate() != null) {
            BigDecimal totalShare = bo.getPlatformShareRate().add(bo.getMerchantShareRate());
            if (totalShare.compareTo(BigDecimal.ONE) != 0) {
                throw new ServiceException("平台分摊比例与商家分摊比例之和必须等于 100%");
            }
        }
        // 按门槛值从小到大排一下序
        List<PromoTierRuleItemBo> sortedTiers = new ArrayList<>(bo.getTierRules());
        sortedTiers.sort(Comparator.comparing(PromoTierRuleItemBo::getThresholdValue));
        for (int i = 0; i < sortedTiers.size(); i++) {
            PromoTierRuleItemBo tier = sortedTiers.get(i);
            if (isFullGift) {
                if (CollUtil.isEmpty(tier.getGiftList())) {
                    throw new ServiceException("满赠活动每个门槛至少配置一个赠品");
                }
            } else if (isDiscount) {
                if (tier.getBenefitValue() == null
                    || tier.getBenefitValue().compareTo(BigDecimal.ZERO) <= 0
                    || tier.getBenefitValue().compareTo(BigDecimal.ONE) >= 0) {
                    throw new ServiceException("折扣率必须在 0~1 之间（不含边界）");
                }
            } else if (isReduce) {
                if (tier.getBenefitValue() == null || tier.getBenefitValue().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServiceException("满减优惠金额必须大于 0");
                }
                if (Long.valueOf(1L).equals(bo.getCalcType())
                    && tier.getBenefitValue().compareTo(tier.getThresholdValue()) >= 0) {
                    throw new ServiceException("优惠金额不能大于或等于门槛金额");
                }
            }
            if (i > 0) {
                PromoTierRuleItemBo prev = sortedTiers.get(i - 1);
                if (tier.getThresholdValue().compareTo(prev.getThresholdValue()) <= 0) {
                    throw new ServiceException("多级阶梯门槛必须递增");
                }
                if (isDiscount && tier.getBenefitValue().compareTo(prev.getBenefitValue()) >= 0) {
                    throw new ServiceException("多级阶梯折扣率必须递减");
                }
                if (isReduce && tier.getBenefitValue().compareTo(prev.getBenefitValue()) <= 0) {
                    throw new ServiceException("多级阶梯优惠金额必须递增");
                }
            }
        }
        PromoActivity add = MapstructUtils.convert(bo, PromoActivity.class);
        add.setTenantId(LoginHelper.getTenantId());
        add.setChannelCode(bo.getChannelCode());
        add.setStatus(0L);
        if (baseMapper.insert(add) <= 0) {
            throw new ServiceException("促销活动创建失败");
        }
        bo.setId(add.getId());
        Long activityId = add.getId();
        List<PromoTierRule> tierRules = new ArrayList<>();
        List<PromoTierGift> giftEntities = new ArrayList<>();
        for (PromoTierRuleItemBo tierDTO : bo.getTierRules()) {
            PromoTierRule tier = new PromoTierRule();
            Long tierRuleId = IdGeneratorUtil.nextLongId();
            tier.setId(tierRuleId);
            tier.setTenantId(add.getTenantId());
            tier.setActivityId(activityId);
            tier.setThresholdValue(tierDTO.getThresholdValue());
            tier.setBenefitValue(tierDTO.getBenefitValue());
            tierRules.add(tier);

            if (isFullGift && CollUtil.isNotEmpty(tierDTO.getGiftList())) {
                for (PromoGiftItemBo giftDTO : tierDTO.getGiftList()) {
                    PromoTierGift gift = new PromoTierGift();
                    gift.setId(IdGeneratorUtil.nextLongId());
                    gift.setTenantId(add.getTenantId());
                    gift.setActivityId(activityId);
                    gift.setTierRuleId(tierRuleId);
                    gift.setGiftSkuId(giftDTO.getGiftSkuId());
                    gift.setGiftCount(giftDTO.getGiftCount());
                    gift.setSort(giftDTO.getSort());
                    giftEntities.add(gift);
                }
            }
        }
        promoTierRuleMapper.batchInsert(tierRules);
        if (CollUtil.isNotEmpty(giftEntities)) {
            promoTierGiftMapper.batchInsert(giftEntities);
        }
        saveScopeTargetsAndUsers(add, bo);
        log.info("促销活动 {} 创建成功，类型 {}，提交待审核", activityId, bo.getPromoType());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillActivity(ExclusivePromoActivityBo bo) {
        if (bo.getAutoCancelMinutes() == null) {
            throw new ServiceException("未支付自动取消时间不能为空");
        }
        return createExclusivePromoActivity(bo, PROMO_TYPE_SECKILL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpecialPriceActivity(ExclusivePromoActivityBo bo) {
        if (bo.getUserScopeType() == null) {
            throw new ServiceException("用户人群范围类型不能为空");
        }
        return createExclusivePromoActivity(bo, PROMO_TYPE_SPECIAL);
    }

    /**
     * 秒杀 / 特价共用落库
     */
    private Long createExclusivePromoActivity(ExclusivePromoActivityBo bo, String promoType) {
        if (bo.getEndTime().before(bo.getStartTime())) {
            throw new ServiceException("活动结束时间不能早于开始时间");
        }
        String tenantId = LoginHelper.getTenantId();
        List<Long> skuIds = bo.getSkuList().stream()
            .map(ExclusiveSkuItem::getSkuId)
            .distinct()
            .collect(Collectors.toList());
        // 同一 SKU 在时间重叠的待审/未开始/进行中秒杀或特价中只能存在一个
        List<Long> conflictedSkuIds = promoGoodsSkuMapper.selectConflictedSkuIds(
            tenantId, skuIds, bo.getStartTime(), bo.getEndTime(), null);
        if (CollUtil.isNotEmpty(conflictedSkuIds)) {
            throw new ServiceException("商品SKU " + conflictedSkuIds + " 在该时间段内已参与其他秒杀/特价活动，请勿重复添加");
        }

        PromoActivity activity = new PromoActivity();
        activity.setTenantId(tenantId);
        activity.setChannelCode(bo.getChannelCode());
        activity.setTitle(bo.getTitle());
        activity.setPromoType(promoType);
        activity.setImgUrl(bo.getImgUrl());
        activity.setStartTime(bo.getStartTime());
        activity.setEndTime(bo.getEndTime());
        if (bo.getAutoCancelMinutes() != null) {
            activity.setAutoCancelMinutes(bo.getAutoCancelMinutes());
        }
        if (bo.getUserScopeType() != null) {
            activity.setUserScopeType(bo.getUserScopeType());
        }
        activity.setStatus(0L);
        baseMapper.insert(activity);
        Long activityId = activity.getId();

        List<PromoGoodsSku> goodsSkuList = new ArrayList<>();
        for (ExclusiveSkuItem item : bo.getSkuList()) {
            PromoGoodsSku goodsSku = new PromoGoodsSku();
            goodsSku.setId(IdGeneratorUtil.nextLongId());
            goodsSku.setTenantId(tenantId);
            goodsSku.setActivityId(activityId);
            goodsSku.setSpuId(item.getSpuId());
            goodsSku.setSkuId(item.getSkuId());
            goodsSku.setPromoPrice(item.getPromoPrice());
            goodsSku.setActivityStock(item.getActivityStock());
            goodsSku.setSoldStock(0L);
            goodsSku.setUserLimitQty(item.getUserLimitQty());
            goodsSkuList.add(goodsSku);
        }
        promoGoodsSkuMapper.batchInsert(goodsSkuList);

        if (PromoScopeUtils.isSpecifiedUserScope(bo.getUserScopeType()) && CollUtil.isNotEmpty(bo.getUserTargetIds())) {
            List<PromoScopeUser> userList = new ArrayList<>();
            for (Long targetId : bo.getUserTargetIds()) {
                PromoScopeUser scopeUser = new PromoScopeUser();
                scopeUser.setId(IdGeneratorUtil.nextLongId());
                scopeUser.setTenantId(tenantId);
                scopeUser.setSourceType(SOURCE_TYPE_PROMO);
                scopeUser.setSourceId(activityId);
                scopeUser.setTargetId(targetId);
                userList.add(scopeUser);
            }
            promoScopeUserMapper.batchInsert(userList);
        }

        log.info("{}活动【{}】创建成功，活动ID：{}", promoType, bo.getTitle(), activityId);
        return activityId;
    }

    private void saveScopeTargetsAndUsers(PromoActivity activity, PromoActivityBo bo) {
        Long activityId = activity.getId();
        List<PromoScopeTarget> targetList = new ArrayList<>();
        if (Long.valueOf(1L).equals(bo.getScopeSupplierType()) && CollUtil.isNotEmpty(bo.getSupplierCodes())) {
            for (String code : bo.getSupplierCodes()) {
                if (StringUtils.isBlank(code)) {
                    continue;
                }
                targetList.add(buildScopeTarget(activity, activityId, PmCacheKeys.CAT_SUPPLIER, code.trim()));
            }
        }
        Long itemCategory = PromoScopeUtils.resolveItemTargetCategory(bo.getScopeItemType());
        if (itemCategory != null && CollUtil.isNotEmpty(bo.getItemIds())) {
            for (Long id : bo.getItemIds()) {
                targetList.add(buildScopeTarget(activity, activityId, itemCategory, String.valueOf(id)));
            }
        }
        if (CollUtil.isNotEmpty(targetList)) {
            for (PromoScopeTarget t : targetList) {
                t.setId(IdGeneratorUtil.nextLongId());
            }
            promoScopeTargetMapper.batchInsert(targetList);
        }

        if (PromoScopeUtils.isSpecifiedUserScope(bo.getUserScopeType()) && CollUtil.isNotEmpty(bo.getUserTargetIds())) {
            List<PromoScopeUser> userList = new ArrayList<>();
            for (Long targetId : bo.getUserTargetIds()) {
                PromoScopeUser userScope = new PromoScopeUser();
                userScope.setId(IdGeneratorUtil.nextLongId());
                userScope.setTenantId(activity.getTenantId());
                userScope.setSourceType(SOURCE_TYPE_PROMO);
                userScope.setSourceId(activityId);
                userScope.setTargetId(targetId);
                userList.add(userScope);
            }
            promoScopeUserMapper.batchInsert(userList);
        }
    }

    private PromoScopeTarget buildScopeTarget(PromoActivity activity, Long activityId, Long category, String targetId) {
        PromoScopeTarget target = new PromoScopeTarget();
        target.setTenantId(activity.getTenantId());
        target.setSourceType(SOURCE_TYPE_PROMO);
        target.setSourceId(activityId);
        target.setTargetCategory(category);
        target.setTargetId(targetId);
        return target;
    }

    @Override
    public Boolean updateByBo(PromoActivityBo bo) {
        PromoActivity update = MapstructUtils.convert(bo, PromoActivity.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 促销活动的统一审核入口   (满减/满折/满赠/秒杀/特价/跨店)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPromoActivity(PromoActivityAuditBo bo) {
        PromoActivity activity = baseMapper.selectById(bo.getId());
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        if (!Long.valueOf(0L).equals(activity.getStatus())) {
            throw new ServiceException("活动非待审核状态");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = toLocalDateTime(activity.getStartTime());
        LocalDateTime endTime = toLocalDateTime(activity.getEndTime());
        activity.setAuditBy(LoginHelper.getUserId());
        activity.setAuditTime(new Date());
        activity.setAuditRemark(bo.getAuditRemark());
        // 审核拒绝
        if (!Boolean.TRUE.equals(bo.getPassFlag())) {
            activity.setStatus(5L);
            baseMapper.updateById(activity);
            log.info("促销活动 {} 审核被驳回", activity.getId());
            return;
        }
        long initStatus;
        if (startTime != null && now.isBefore(startTime)) {
            initStatus = 1L;
        } else if (endTime == null || now.isBefore(endTime)) {
            initStatus = 2L;
        } else {
            initStatus = 3L;
        }
        activity.setStatus(initStatus);
        baseMapper.updateById(activity);
        String promoType = activity.getPromoType();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                boolean warmCache = Long.valueOf(1L).equals(activity.getStatus())
                    || Long.valueOf(2L).equals(activity.getStatus());
                if (PROMO_TYPE_SECKILL.equals(promoType)) {
                    if (warmCache) {
                        warmUpSeckillCache(activity.getId());
                    }
                    scheduleSeckillDelayMq(activity, now, startTime, endTime);
                } else if (PROMO_TYPE_SPECIAL.equals(promoType)) {
                    if (warmCache) {
                        cacheSpecialPricePromo(activity.getId());
                    }
                    scheduleSpecialPriceDelayMq(activity, now, startTime, endTime);
                } else if (PROMO_TYPE_CROSS_REDUCE.equals(promoType)) {
                    if (warmCache) {
                        cacheCrossFullReducePromo(activity.getId());
                    }
                    scheduleCrossLadderDelayMq(activity, now, startTime, endTime,
                        CouponMqConstants.KEY_CROSS_REDUCE_START_DELAY,
                        CouponMqConstants.KEY_CROSS_REDUCE_END_DELAY);
                } else if (PROMO_TYPE_CROSS_DISCOUNT.equals(promoType)) {
                    if (warmCache) {
                        cacheCrossFullDiscountPromo(activity.getId());
                    }
                    scheduleCrossLadderDelayMq(activity, now, startTime, endTime,
                        CouponMqConstants.KEY_CROSS_DISCOUNT_START_DELAY,
                        CouponMqConstants.KEY_CROSS_DISCOUNT_END_DELAY);
                } else {
                    if (warmCache) {
                        cachePromoActivity(activity.getId());
                    }
                    schedulePromoActivityDelayMq(activity, now, startTime, endTime);
                }
            }
        });
        log.info("促销活动 {} 审核通过，类型 {}，初始状态: {}", activity.getId(), activity.getPromoType(), activity.getStatus());
    }

    private void scheduleCrossLadderDelayMq(PromoActivity activity, LocalDateTime now,
                                            LocalDateTime startTime, LocalDateTime endTime,
                                            String startRoutingKey, String endRoutingKey) {
        Long activityId = activity.getId();
        if (Long.valueOf(1L).equals(activity.getStatus())) {
            long startDelay = Duration.between(now, startTime).toMillis();
            if (startDelay > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    startRoutingKey,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(startDelay);
                        return message;
                    }
                );
                log.info("跨店活动 {} 将在 {} 毫秒后自动开启，类型 {}", activityId, startDelay, activity.getPromoType());
            }
        }
        if (Long.valueOf(1L).equals(activity.getStatus()) || Long.valueOf(2L).equals(activity.getStatus())) {
            long endDelay = Duration.between(now, endTime).toMillis();
            if (endDelay > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    endRoutingKey,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(endDelay);
                        return message;
                    }
                );
                log.info("跨店活动 {} 将在 {} 毫秒后自动结束，类型 {}", activityId, endDelay, activity.getPromoType());
            }
        }
    }

    /**
     * 审核通过后，给特价发送开关延时 MQ
     */
    private void scheduleSpecialPriceDelayMq(PromoActivity activity, LocalDateTime now,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        Long activityId = activity.getId();
        if (Long.valueOf(1L).equals(activity.getStatus())) {
            long startDelay = Duration.between(now, startTime).toMillis();
            if (startDelay > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_SPECIAL_PRICE_START_DELAY,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(startDelay);
                        return message;
                    }
                );
                log.info("特价活动 {} 将在 {} 毫秒后自动开启", activityId, startDelay);
            }
        }
        if (Long.valueOf(1L).equals(activity.getStatus()) || Long.valueOf(2L).equals(activity.getStatus())) {
            long endDelay = Duration.between(now, endTime).toMillis();
            if (endDelay > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_SPECIAL_PRICE_END_DELAY,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(endDelay);
                        return message;
                    }
                );
                log.info("特价活动 {} 将在 {} 毫秒后自动结束", activityId, endDelay);
            }
        }
    }

    /**
     * 审核通过后，给秒杀发送开关延时 MQ
     */
    private void scheduleSeckillDelayMq(PromoActivity activity, LocalDateTime now, LocalDateTime startTime, LocalDateTime endTime) {
        Long activityId = activity.getId();
        if (Long.valueOf(1L).equals(activity.getStatus())) {
            // 现在距离开始还有多少毫秒
            long startDelay = Duration.between(now, startTime).toMillis();
            if (startDelay > 0) {
                // 发送延时开始MQ
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_SECKILL_START_DELAY,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(startDelay);
                        return message;
                    }
                );
                log.info("秒杀活动 {} 将在 {} 毫秒后自动开启", activityId, startDelay);
            }
        }
        // 发送延时结束MQ
        if (Long.valueOf(1L).equals(activity.getStatus()) || Long.valueOf(2L).equals(activity.getStatus())) {
            // 现在距离结束还有多少秒
            long endDelay = Duration.between(now, endTime).toMillis();
            if (endDelay > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_SECKILL_END_DELAY,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(endDelay);
                        return message;
                    }
                );
                log.info("秒杀活动 {} 将在 {} 毫秒后自动结束", activityId, endDelay);
            }
        }
    }


    private void schedulePromoActivityDelayMq(PromoActivity activity, LocalDateTime now,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        Long activityId = activity.getId();
        if (Long.valueOf(1L).equals(activity.getStatus())) {
            long ttlToStart = Duration.between(now, startTime).toMillis();
            if (ttlToStart > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_PROMO_ACTIVITY_START_DELAY,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlToStart);
                        return message;
                    }
                );
                log.info("满减活动 {} 将在 {} 毫秒后自动变更为 [进行中]", activityId, ttlToStart);
            }
        }
        if (Long.valueOf(1L).equals(activity.getStatus()) || Long.valueOf(2L).equals(activity.getStatus())) {
            long ttlToEnd = Duration.between(now, endTime).toMillis();
            if (ttlToEnd > 0) {
                rabbitTemplate.convertAndSend(
                    CouponMqConstants.EXCHANGE_PM_DELAY,
                    CouponMqConstants.KEY_PROMO_ACTIVITY_END_DELAY,
                    activityId,
                    message -> {
                        message.getMessageProperties().setDelayLong(ttlToEnd);
                        return message;
                    }
                );
                log.info("满减活动 {} 将在 {} 毫秒后自动变更为 [已结束]", activityId, ttlToEnd);
            }
        }
    }

    /**
     * 预热满减/满折/满赠活动缓存
     */
    @Override
    public void cachePromoActivity(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        if (activity == null) {
            return;
        }
        boolean live = Long.valueOf(2L).equals(activity.getStatus());
        long ttlSeconds = (activity.getEndTime().getTime() - System.currentTimeMillis()) / 1000 + 86400L;
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.promoDetail(tenant, channel, activityId),
            JsonUtils.toJsonString(activity),
            ttlSeconds,
            TimeUnit.SECONDS);
        List<PromoTierRule> tierRules = promoTierRuleMapper.selectListByActivityId(activityId);
        tierRules.sort(Comparator.comparing(PromoTierRule::getThresholdValue));
        if (PROMO_TYPE_FULL_GIFT.equals(activity.getPromoType())) {
            List<PromoTierGift> allGifts = promoTierGiftMapper.selectListByActivityId(activityId);
            Map<Long, List<PromoTierGift>> giftGroupMap = allGifts.stream().collect(Collectors.groupingBy(PromoTierGift::getTierRuleId));
            for (PromoTierRule rule : tierRules) {
                rule.setGiftList(giftGroupMap.getOrDefault(rule.getId(), Collections.emptyList()));
            }
        }
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.promoTiers(tenant, channel, activityId),
            JsonUtils.toJsonString(tierRules),
            ttlSeconds,
            TimeUnit.SECONDS);
        List<PromoScopeTarget> targets = promoScopeTargetMapper.selectBySourceId(activityId, SOURCE_TYPE_PROMO);
        cachePromoTargetScopes(activity, targets, ttlSeconds, live);
        cachePromoUserScopes(activity, activityId, ttlSeconds);
        String idStr = String.valueOf(activityId);
        stringRedisTemplate.opsForSet().add(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        if (live) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        }
        log.info("促销活动 {} 缓存预热完成，类型 {}，status={}", activityId, activity.getPromoType(), activity.getStatus());
    }


    /**
     * 店内满减/满折/满赠，以及跨店满减/满折活动结束清除Redis
     */
    @Override
    public void clearPromoActivityCache(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        List<PromoScopeTarget> targets = promoScopeTargetMapper.selectBySourceId(activityId, SOURCE_TYPE_PROMO);
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        stringRedisTemplate.delete(List.of(
            PmCacheKeys.promoDetail(tenant, channel, activityId),
            PmCacheKeys.promoTiers(tenant, channel, activityId),
            PmCacheKeys.promoBudget(tenant, channel, activityId),
            PmCacheKeys.promoStock(tenant, channel, activityId),
            PmCacheKeys.promoUserLimit(tenant, channel, activityId),
            PmCacheKeys.promoUser(tenant, channel, "white", activityId),
            PmCacheKeys.promoUser(tenant, channel, "black", activityId),
            PmCacheKeys.promoLevel(tenant, channel, "white", activityId),
            PmCacheKeys.promoLevel(tenant, channel, "black", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "supplier", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "site", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "merchant", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "category", "white", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "category", "black", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "brand", "white", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "brand", "black", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "sku", "white", activityId),
            PmCacheKeys.promoTarget(tenant, channel, "sku", "black", activityId)
        ));
        String idStr = String.valueOf(activityId);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoScopeAll(tenant, channel), idStr);
        for (PromoScopeTarget target : targets) {
            if (!PmCacheKeys.isItemCategory(target.getTargetCategory())) {
                continue;
            }
            stringRedisTemplate.opsForSet().remove(
                PmCacheKeys.promoScope(tenant, channel, target.getTargetCategory(), target.getTargetId()),
                idStr);
        }
        log.info("促销活动 {} 缓存已清理", activityId);
    }


    /**
     * 负责把活动的商品/供应商圈货范围写进 Redis
     */
    private void cachePromoTargetScopes(PromoActivity activity, List<PromoScopeTarget> targets,
                                        long ttlSeconds, boolean writeDiscoveryScope) {
        Long activityId = activity.getId();
        String idStr = String.valueOf(activityId);
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        // 是否全场
        boolean allItems = Long.valueOf(0L).equals(activity.getScopeItemType());
        Long itemMode = activity.getItemParticipateType();
        // 指定了商品范围，切是不参与
        boolean itemBlack = !allItems && Long.valueOf(2L).equals(itemMode);
        // 进行中才进发现池，避免未开始活动被结算召回
        if (writeDiscoveryScope && (allItems || itemBlack)) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.promoScopeAll(tenant, channel), idStr);
        }
        if (CollUtil.isEmpty(targets)) {
            return;
        }
        String itemModeTag = PmCacheKeys.modeTag(itemMode);
        for (PromoScopeTarget target : targets) {
            Long cat = target.getTargetCategory();
            String dim = PmCacheKeys.targetDim(cat);
            if (Long.valueOf(PmCacheKeys.CAT_SUPPLIER).equals(cat)) {
                if (!Long.valueOf(1L).equals(activity.getScopeSupplierType())) {
                    continue;
                }
                String key = PmCacheKeys.promoTarget(tenant, channel, dim, activityId);
                // 缓存供应商范围
                stringRedisTemplate.opsForSet().add(key, target.getTargetId());
                stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
                continue;
            }
            // 非全场缓存商品范围
            if (!allItems && PmCacheKeys.isItemCategory(cat)) {
                String forwardKey = PmCacheKeys.promoTarget(tenant, channel, dim, itemModeTag, activityId);
                stringRedisTemplate.opsForSet().add(forwardKey, target.getTargetId());
                stringRedisTemplate.expire(forwardKey, ttlSeconds, TimeUnit.SECONDS);
                if (writeDiscoveryScope && !itemBlack) {
                    stringRedisTemplate.opsForSet().add(PmCacheKeys.promoScope(tenant, channel, cat, target.getTargetId()), idStr);
                }
            }
        }
    }

    /**
     * 把活动的人群范围名单预热到 Redis
     */
    private void cachePromoUserScopes(PromoActivity activity, Long activityId, long ttlSeconds) {
        if (!PromoScopeUtils.isSpecifiedUserScope(activity.getUserScopeType())) {
            return;
        }
        List<PromoScopeUser> scopeUsers = promoScopeUserMapper.selectListBySourceId(activityId, SOURCE_TYPE_PROMO);
        if (CollUtil.isEmpty(scopeUsers)) {
            return;
        }
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        String modeTag = PmCacheKeys.modeTag(activity.getUserParticipateType());
        boolean isLevel = Long.valueOf(2L).equals(activity.getUserScopeType());
        String key = isLevel
            ? PmCacheKeys.promoLevel(tenant, channel, modeTag, activityId)
            : PmCacheKeys.promoUser(tenant, channel, modeTag, activityId);
        for (PromoScopeUser scopeUser : scopeUsers) {
            stringRedisTemplate.opsForSet().add(key, String.valueOf(scopeUser.getTargetId()));
        }
        stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * 预热秒杀缓存
     */
    @Override
    public void warmUpSeckillCache(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        if (activity == null) {
            return;
        }
        boolean live = Long.valueOf(2L).equals(activity.getStatus());
        // TTL = 距结束秒数 + 1 天缓冲
        long ttlSeconds = (activity.getEndTime().getTime() - System.currentTimeMillis()) / 1000 + 86400L;
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        // 缓存活动主信息
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.promoDetail(tenant, channel, activityId),
            JsonUtils.toJsonString(activity),
            ttlSeconds,
            TimeUnit.SECONDS);
        List<PromoGoodsSku> skuList = promoGoodsSkuMapper.selectListByActivityId(activityId);
        String idStr = String.valueOf(activityId);
        String skuWhiteKey = PmCacheKeys.promoTarget(tenant, channel, "sku", "white", activityId);
        for (PromoGoodsSku sku : skuList) {
            Long skuId = sku.getSkuId();
            // 缓存秒杀商品
            stringRedisTemplate.opsForValue().set(
                PmCacheKeys.seckillSku(tenant, channel, activityId, skuId),
                JsonUtils.toJsonString(sku),
                ttlSeconds,
                TimeUnit.SECONDS);
            // 缓存活动配额剩余
            stringRedisTemplate.opsForValue().set(
                PmCacheKeys.seckillStock(tenant, channel, activityId, skuId),
                String.valueOf(sku.getActivityStock()),
                ttlSeconds,
                TimeUnit.SECONDS);
            // 本活动商品白名单 Set
            stringRedisTemplate.opsForSet().add(skuWhiteKey, String.valueOf(skuId));
            // 倒排索引仅进行中写入，避免未开始被结算召回
            if (live) {
                stringRedisTemplate.opsForSet().add(
                    PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_SKU, skuId),
                    idStr);
            }
        }
        if (CollUtil.isNotEmpty(skuList)) {
            stringRedisTemplate.expire(skuWhiteKey, ttlSeconds, TimeUnit.SECONDS);
        }
        stringRedisTemplate.opsForSet().add(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        if (live) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        }
        publishExclusivePromoEsUpsert(activity, PROMO_TYPE_SECKILL, skuList);
        log.info("[秒杀预热] 活动 {} status={} 已写入 detail/sku/stock/display_ids；进行中才写 scope/active_ids",
            activityId, activity.getStatus());
    }

    /**
     * 秒杀活动结束后，清理事先预热的Redis
     */
    @Override
    public void clearSeckillCache(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        List<PromoGoodsSku> skuList = promoGoodsSkuMapper.selectListByActivityId(activityId);
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(PmCacheKeys.promoDetail(tenant, channel, activityId));
        keysToDelete.add(PmCacheKeys.promoTarget(tenant, channel, "sku", "white", activityId));
        String idStr = String.valueOf(activityId);
        for (PromoGoodsSku sku : skuList) {
            Long skuId = sku.getSkuId();
            keysToDelete.add(PmCacheKeys.seckillSku(tenant, channel, activityId, skuId));
            keysToDelete.add(PmCacheKeys.seckillStock(tenant, channel, activityId, skuId));
            keysToDelete.add(PmCacheKeys.seckillUserLimit(tenant, channel, activityId, skuId));
            stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_SKU, skuId), idStr);
        }
        stringRedisTemplate.delete(keysToDelete);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        publishExclusivePromoEsClear(activity, PROMO_TYPE_SECKILL, skuList);
        log.info("[秒杀清理] 活动 {} 缓存已清理", activityId);
    }

    /**
     * 预热特价缓存
     */
    @Override
    public void cacheSpecialPricePromo(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        if (activity == null) {
            return;
        }
        boolean live = Long.valueOf(2L).equals(activity.getStatus());
        long ttlSeconds = (activity.getEndTime().getTime() - System.currentTimeMillis()) / 1000 + 86400L;
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        // 缓存活动主信息
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.promoDetail(tenant, channel, activityId),
            JsonUtils.toJsonString(activity),
            ttlSeconds,
            TimeUnit.SECONDS);
        List<PromoGoodsSku> goodsSkuList = promoGoodsSkuMapper.selectListByActivityId(activityId);
        String idStr = String.valueOf(activityId);
        String skuWhiteKey = PmCacheKeys.promoTarget(tenant, channel, "sku", "white", activityId);
        for (PromoGoodsSku sku : goodsSkuList) {
            Long skuId = sku.getSkuId();
            // 缓存特价商品
            stringRedisTemplate.opsForValue().set(
                PmCacheKeys.specialSku(tenant, channel, activityId, skuId),
                JsonUtils.toJsonString(sku),
                ttlSeconds,
                TimeUnit.SECONDS);
            // 缓存活动库存配额
            stringRedisTemplate.opsForValue().set(
                PmCacheKeys.specialStock(tenant, channel, activityId, skuId),
                String.valueOf(sku.getActivityStock()),
                ttlSeconds,
                TimeUnit.SECONDS);
            // 缓存本活动允许的 SKU 白名单 Set
            stringRedisTemplate.opsForSet().add(skuWhiteKey, String.valueOf(skuId));
            if (live) {
                stringRedisTemplate.opsForSet().add(
                    PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_SKU, skuId),
                    idStr);
            }
        }
        if (CollUtil.isNotEmpty(goodsSkuList)) {
            stringRedisTemplate.expire(skuWhiteKey, ttlSeconds, TimeUnit.SECONDS);
        }
        if (PromoScopeUtils.isSpecifiedUserScope(activity.getUserScopeType())) {
            List<PromoScopeUser> scopeUsers = promoScopeUserMapper.selectListBySourceId(activityId, SOURCE_TYPE_PROMO);
            if (CollUtil.isNotEmpty(scopeUsers)) {
                String modeTag = PmCacheKeys.modeTag(activity.getUserParticipateType());
                boolean isLevel = Long.valueOf(2L).equals(activity.getUserScopeType());
                String scopeKey = isLevel
                    ? PmCacheKeys.promoLevel(tenant, channel, modeTag, activityId)
                    : PmCacheKeys.promoUser(tenant, channel, modeTag, activityId);
                for (PromoScopeUser scopeUser : scopeUsers) {
                    // 缓存人群范围
                    stringRedisTemplate.opsForSet().add(scopeKey, String.valueOf(scopeUser.getTargetId()));
                }
                stringRedisTemplate.expire(scopeKey, ttlSeconds, TimeUnit.SECONDS);
            }
        }
        stringRedisTemplate.opsForSet().add(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        if (live) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        }
        publishExclusivePromoEsUpsert(activity, PROMO_TYPE_SPECIAL, goodsSkuList);
        log.info("[特价预热] 活动 {} status={} 已写入 detail/sku/stock/人群/display_ids；进行中才写 scope/active_ids",
            activityId, activity.getStatus());
    }

    @Override
    public void clearSpecialPriceCache(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        List<PromoGoodsSku> goodsSkuList = promoGoodsSkuMapper.selectListByActivityId(activityId);
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(PmCacheKeys.promoDetail(tenant, channel, activityId));
        keysToDelete.add(PmCacheKeys.promoTarget(tenant, channel, "sku", "white", activityId));
        keysToDelete.add(PmCacheKeys.promoUser(tenant, channel, "white", activityId));
        keysToDelete.add(PmCacheKeys.promoUser(tenant, channel, "black", activityId));
        keysToDelete.add(PmCacheKeys.promoLevel(tenant, channel, "white", activityId));
        keysToDelete.add(PmCacheKeys.promoLevel(tenant, channel, "black", activityId));
        String idStr = String.valueOf(activityId);
        for (PromoGoodsSku sku : goodsSkuList) {
            Long skuId = sku.getSkuId();
            keysToDelete.add(PmCacheKeys.specialSku(tenant, channel, activityId, skuId));
            keysToDelete.add(PmCacheKeys.specialStock(tenant, channel, activityId, skuId));
            keysToDelete.add(PmCacheKeys.specialUserLimit(tenant, channel, activityId, skuId));
            stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_SKU, skuId), idStr);
        }
        stringRedisTemplate.delete(keysToDelete);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        stringRedisTemplate.opsForSet().remove(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        publishExclusivePromoEsClear(activity, PROMO_TYPE_SPECIAL, goodsSkuList);
        log.info("[特价清理] 活动 {} 缓存已清理", activityId);
    }

    /**
     * 预热跨店满减
     */
    @Override
    public void cacheCrossFullReducePromo(Long activityId) {
        cacheCrossLadderPromo(activityId);
    }

    @Override
    public void clearCrossFullReduceCache(Long activityId) {
        clearPromoActivityCache(activityId);
        log.info("跨店满减活动 [{}] Redis 缓存及共享索引清理完毕", activityId);
    }


    @Override
    public void cacheCrossFullDiscountPromo(Long activityId) {
        cacheCrossLadderPromo(activityId);
    }

    @Override
    public void clearCrossFullDiscountCache(Long activityId) {
        clearPromoActivityCache(activityId);
        log.info("跨店满折活动 [{}] Redis 缓存及共享索引清理完毕", activityId);
    }

    private void cacheCrossLadderPromo(Long activityId) {
        PromoActivity activity = baseMapper.selectById(activityId);
        if (activity == null) {
            return;
        }
        boolean live = Long.valueOf(2L).equals(activity.getStatus());
        long ttlSeconds = (activity.getEndTime().getTime() - System.currentTimeMillis()) / 1000 + 86400L;
        String tenant = activity.getTenantId();
        String channel = activity.getChannelCode();
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.promoDetail(tenant, channel, activityId),
            JsonUtils.toJsonString(activity),
            ttlSeconds,
            TimeUnit.SECONDS);
        List<PromoTierRule> tierRules = promoTierRuleMapper.selectListByActivityId(activityId);
        tierRules.sort(Comparator.comparing(PromoTierRule::getThresholdValue));
        // 缓存门槛规则
        stringRedisTemplate.opsForValue().set(
            PmCacheKeys.promoTiers(tenant, channel, activityId),
            JsonUtils.toJsonString(tierRules),
            ttlSeconds,
            TimeUnit.SECONDS);
        if (activity.getBudgetTotal() != null) {
            long budgetFen = MoneyFenUtils.toFen(activity.getBudgetTotal());
            // 缓存活动预算
            stringRedisTemplate.opsForValue().set(
                PmCacheKeys.promoBudget(tenant, channel, activityId),
                String.valueOf(budgetFen),
                ttlSeconds,
                TimeUnit.SECONDS);
        }
        List<PromoScopeTarget> targets = promoScopeTargetMapper.selectBySourceId(activityId, SOURCE_TYPE_PROMO);
        cachePromoTargetScopes(activity, targets, ttlSeconds, live);
        cachePromoUserScopes(activity, activityId, ttlSeconds);
        String idStr = String.valueOf(activityId);
        stringRedisTemplate.opsForSet().add(PmCacheKeys.promoDisplayIds(tenant, channel), idStr);
        if (live) {
            stringRedisTemplate.opsForSet().add(PmCacheKeys.promoActiveIds(tenant, channel), idStr);
        }
        log.info("跨店活动 {} 缓存预热完成，类型 {}，status={}", activityId, activity.getPromoType(), activity.getStatus());
    }

    private long calculateSpecialTtlSeconds(Date now, Date endTime, long bufferSeconds) {
        long remainSeconds = (endTime.getTime() - now.getTime()) / 1000;
        if (remainSeconds <= 0) {
            return 3600L;
        }
        return remainSeconds + bufferSeconds;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static final int ES_PROMO_MQ_BATCH = 200;

    private void publishExclusivePromoEsUpsert(PromoActivity activity, String promoType, List<PromoGoodsSku> skuList) {
        if (activity == null || CollUtil.isEmpty(skuList)) {
            return;
        }
        Integer status = activity.getStatus() == null ? null : activity.getStatus().intValue();
        List<ExclusivePromoEsChangeEvent.Item> items = skuList.stream()
            .filter(s -> s.getSkuId() != null)
            .map(s -> ExclusivePromoEsChangeEvent.Item.builder()
                .skuId(s.getSkuId())
                .promoPrice(s.getPromoPrice())
                .build())
            .collect(Collectors.toList());
        publishExclusivePromoEsBatches(ExclusivePromoEsChangeEvent.ACTION_UPSERT, activity, promoType, status, items);
    }

    private void publishExclusivePromoEsClear(PromoActivity activity, String promoType, List<PromoGoodsSku> skuList) {
        if (activity == null || CollUtil.isEmpty(skuList)) {
            return;
        }
        List<ExclusivePromoEsChangeEvent.Item> items = skuList.stream()
            .filter(s -> s.getSkuId() != null)
            .map(s -> ExclusivePromoEsChangeEvent.Item.builder().skuId(s.getSkuId()).build())
            .collect(Collectors.toList());
        publishExclusivePromoEsBatches(ExclusivePromoEsChangeEvent.ACTION_CLEAR, activity, promoType, null, items);
    }

    private void publishExclusivePromoEsBatches(String action, PromoActivity activity, String promoType,
                                                Integer promoStatus, List<ExclusivePromoEsChangeEvent.Item> items) {
        for (int from = 0; from < items.size(); from += ES_PROMO_MQ_BATCH) {
            int to = Math.min(from + ES_PROMO_MQ_BATCH, items.size());
            ExclusivePromoEsChangeEvent event = ExclusivePromoEsChangeEvent.builder()
                .action(action)
                .tenantId(activity.getTenantId())
                .channelCode(activity.getChannelCode())
                .promoType(promoType)
                .promoActivityId(activity.getId())
                .promoStatus(promoStatus)
                .promoStartTime(activity.getStartTime())
                .promoEndTime(activity.getEndTime())
                .items(items.subList(from, to))
                .build();
            try {
                rabbitTemplate.convertAndSend(
                    GoodsExclusivePromoEsMqConstants.EXCHANGE_GOODS_DIRECT,
                    GoodsExclusivePromoEsMqConstants.KEY_EXCLUSIVE_PROMO_ES,
                    event);
            } catch (Exception e) {
                log.error("[排他促销ES] 发送失败 action={} activityId={} batch={}-{}",
                    action, activity.getId(), from, to, e);
            }
        }
        log.info("[排他促销ES] 已发送 action={} activityId={} skuCount={}",
            action, activity.getId(), items.size());
    }
}
