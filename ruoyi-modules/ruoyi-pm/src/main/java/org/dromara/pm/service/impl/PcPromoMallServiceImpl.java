package org.dromara.pm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.goods.api.domain.RemoteGoodsSkuView;
import org.dromara.goods.api.domain.RemoteGoodsSkuSearchBo;
import org.dromara.goods.api.domain.RemoteGoodsSkuSearchResult;
import org.dromara.goods.api.domain.RemoteMasterSkuBo;
import org.dromara.pm.constant.PmCacheKeys;
import org.dromara.pm.domain.CouponGiftGrantRule;
import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.domain.PromoTierRule;
import org.dromara.pm.domain.bo.PcCouponCenterQueryBo;
import org.dromara.pm.domain.bo.PcCouponClaimBo;
import org.dromara.pm.domain.bo.PcMyCouponQueryBo;
import org.dromara.pm.domain.bo.PcPdpMarketingQueryBo;
import org.dromara.pm.domain.bo.PcPromoGoodsQueryBo;
import org.dromara.pm.domain.bo.PcPromoQueryBo;
import org.dromara.pm.domain.bo.PcSeckillZoneQueryBo;
import org.dromara.pm.domain.vo.CouponGiftGrantRuleVo;
import org.dromara.pm.domain.vo.CouponTemplateVo;
import org.dromara.pm.domain.vo.CouponUserRelVo;
import org.dromara.pm.domain.vo.PcPdpMarketingVo;
import org.dromara.pm.domain.vo.PcPromoActivityVo;
import org.dromara.pm.domain.vo.PcPromoGoodsVo;
import org.dromara.pm.domain.vo.PcSeckillZoneGoodsVo;
import org.dromara.pm.domain.vo.PromoTierRuleVo;
import org.dromara.pm.mapper.CouponUserRelMapper;
import org.dromara.pm.service.IPcPromoMallService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.pm.utils.CouponValidTimeUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class PcPromoMallServiceImpl implements IPcPromoMallService {

    private static final String MEMBER_CODE_PLATFORM = "PLATFORM";
    private static final String TYPE_SECKILL = "SECKILL";
    private static final String TYPE_SPECIAL = "SPECIAL_PRICE";
    private static final String TYPE_COUPON_GIFT = "COUPON_GIFT";
    private static final String TYPE_CROSS_REDUCE = "CROSS_FULL_REDUCE";
    private static final String TYPE_CROSS_DISCOUNT = "CROSS_FULL_DISCOUNT";
    private static final String GRANT_NORMAL = "NORMAL";

    /** 用户券：未使用 */
    private static final long USER_COUPON_UNUSED = 0L;
    /** 用户券：已使用 */
    private static final long USER_COUPON_USED = 1L;
    /** 用户券：已过期 */
    private static final long USER_COUPON_EXPIRED = 2L;
    /** 用户券：已作废 */
    private static final long USER_COUPON_VOIDED = 3L;

    private final StringRedisTemplate stringRedisTemplate;
    private final CouponUserRelMapper couponUserRelMapper;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    private DefaultRedisScript<Long> couponClaimScript;

    @PostConstruct
    public void initCouponClaimScript() {
        couponClaimScript = new DefaultRedisScript<>();
        couponClaimScript.setResultType(Long.class);
        couponClaimScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/coupon_claim.lua")));
    }


    @Override
    public TableDataInfo<PcPromoActivityVo> queryPcPromoActivityList(PcPromoQueryBo query) {
        String tenant = TenantHelper.getTenantId();
        String channel = query.getChannelCode();
        boolean login = LoginHelper.isLogin();
        Long userId = login ? LoginHelper.getUserId() : null;
        Long userLevelId = login ? LoginHelper.getDistLevelId() : null;
        Date now = new Date();
        String promoTypeFilter = query.getPromoType();
        int statusType = query.getStatusType();
        Set<String> displayPromoIds = stringRedisTemplate.opsForSet().members(PmCacheKeys.promoDisplayIds(tenant, channel));
        Set<String> displayGrantIds = stringRedisTemplate.opsForSet().members(PmCacheKeys.couponGrantDisplayIds(tenant, channel));
        if (CollUtil.isEmpty(displayPromoIds) && CollUtil.isEmpty(displayGrantIds)) {
            return TableDataInfo.build();
        }
        List<PcPromoActivityVo> allList = new ArrayList<>();
        // 没指定类型，或指定的不是满赠
        boolean queryPromo = StringUtils.isBlank(promoTypeFilter) || !TYPE_COUPON_GIFT.equalsIgnoreCase(promoTypeFilter);
        // 没指定类型，或指定的就是满赠
        boolean queryGrant = StringUtils.isBlank(promoTypeFilter) || TYPE_COUPON_GIFT.equalsIgnoreCase(promoTypeFilter);
        if (queryPromo && CollUtil.isNotEmpty(displayPromoIds)) {
            appendPromoActivityVos(allList, displayPromoIds, promoTypeFilter, statusType, now, userId, userLevelId, tenant, channel);
        }
        if (queryGrant && CollUtil.isNotEmpty(displayGrantIds)) {
            appendGrantActivityVos(allList, displayGrantIds, statusType, now, userId, userLevelId, tenant, channel);
        }
        if (allList.isEmpty()) {
            return TableDataInfo.build();
        }
        allList.sort(this::compareActivityDisplay);
        return slicePage(allList, query.getPageNum(), query.getPageSize());
    }

    /**
     * 把 促销活动从 Redis 捞出来，筛完后塞进列表 out。
     */
    private void appendPromoActivityVos(List<PcPromoActivityVo> out, Set<String> idStrs, String promoTypeFilter,
                                        int statusType, Date now,
                                        Long userId, Long userLevelId, String tenant, String channel) {
        List<Long> ids = parseLongIds(idStrs);
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<String> keys = ids.stream()
            .map(id -> PmCacheKeys.promoDetail(tenant, channel, id))
            .collect(Collectors.toList());
        for (String json : CollUtil.emptyIfNull(stringRedisTemplate.opsForValue().multiGet(keys))) {
            if (StringUtils.isBlank(json)) {
                continue;
            }
            PromoActivity activity = JsonUtils.parseObject(json, PromoActivity.class);
            if (activity == null) {
                continue;
            }
            String promoType = activity.getPromoType();
            if (StringUtils.isNotBlank(promoTypeFilter) && !promoTypeFilter.equalsIgnoreCase(promoType)) {
                continue;
            }
            Long status = activity.getStatus();
            if (statusType != 0 && !Long.valueOf(statusType).equals(status)) {
                continue;
            }
            if (now.after(activity.getEndTime())) {
                continue;
            }
            if (!checkUserEligibility(activity.getUserScopeType(), activity.getUserParticipateType(),
                ScopeBiz.PROMO, activity.getId(), userId, userLevelId, tenant, channel)) {
                continue;
            }
            out.add(toPromoActivityVo(activity, tenant, channel));
        }
    }

    private void appendGrantActivityVos(List<PcPromoActivityVo> out, Set<String> idStrs, int statusType,
                                        Date now, Long userId, Long userLevelId, String tenant, String channel) {
        List<Long> ids = parseLongIds(idStrs);
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<String> keys = ids.stream()
            .map(id -> PmCacheKeys.couponGrantDetail(tenant, channel, id))
            .collect(Collectors.toList());
        for (String json : CollUtil.emptyIfNull(stringRedisTemplate.opsForValue().multiGet(keys))) {
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponGiftGrantRule rule = JsonUtils.parseObject(json, CouponGiftGrantRule.class);
            if (rule == null) {
                continue;
            }
            Long status = rule.getStatus();
            if (statusType != 0 && !Long.valueOf(statusType).equals(status)) {
                continue;
            }
            if (now.after(rule.getEndTime())) {
                continue;
            }
            if (!checkUserEligibility(rule.getUserScopeType(), rule.getUserParticipateType(),
                ScopeBiz.GRANT, rule.getId(), userId, userLevelId, tenant, channel)) {
                continue;
            }
            out.add(toGrantActivityVo(rule, tenant, channel));
        }
    }

    private PcPromoActivityVo toPromoActivityVo(PromoActivity activity, String tenant, String channel) {
        PcPromoActivityVo vo = new PcPromoActivityVo();
        vo.setId(activity.getId());
        vo.setTenantCode(tenant);
        vo.setChannelCode(channel);
        vo.setTitle(activity.getTitle());
        vo.setPromoType(activity.getPromoType());
        vo.setCalcType(activity.getCalcType());
        vo.setImgUrl(activity.getImgUrl());
        vo.setMemberCode(activity.getMemberCode());
        vo.setMemberName(activity.getMemberName());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setScopeItemType(activity.getScopeItemType());
        vo.setStatus(activity.getStatus());
        vo.setIsCap(activity.getIsCap());
        fillPromoTierRules(vo, activity.getId(), tenant, channel);
        return vo;
    }

    private void fillPromoTierRules(PcPromoActivityVo vo, Long activityId, String tenant, String channel) {
        String tiersJson = stringRedisTemplate.opsForValue()
            .get(PmCacheKeys.promoTiers(tenant, channel, activityId));
        if (StringUtils.isBlank(tiersJson)) {
            return;
        }
        List<PromoTierRuleVo> tierRules = new ArrayList<>();
        for (PromoTierRule tier : JsonUtils.parseArray(tiersJson, PromoTierRule.class)) {
            PromoTierRuleVo tierVo = new PromoTierRuleVo();
            tierVo.setId(tier.getId());
            tierVo.setActivityId(activityId);
            tierVo.setThresholdValue(tier.getThresholdValue());
            tierVo.setBenefitValue(tier.getBenefitValue());
            tierRules.add(tierVo);
        }
        vo.setTierRules(tierRules);
    }

    private PcPromoActivityVo toGrantActivityVo(CouponGiftGrantRule rule, String tenant, String channel) {
        PcPromoActivityVo vo = new PcPromoActivityVo();
        vo.setId(rule.getId());
        vo.setTenantCode(tenant);
        vo.setChannelCode(channel);
        vo.setTitle(rule.getTitle());
        vo.setPromoType(TYPE_COUPON_GIFT);
        vo.setCalcType(1L);
        vo.setImgUrl(rule.getImgUrl());
        vo.setMemberCode(rule.getMemberCode());
        vo.setMemberName(rule.getMemberName());
        vo.setStartTime(rule.getStartTime());
        vo.setEndTime(rule.getEndTime());
        vo.setScopeItemType(0L);
        vo.setStatus(rule.getStatus());
        vo.setMinOrderAmount(rule.getMinOrderAmount());
        vo.setCouponTemplateId(rule.getCouponTemplateId());
        vo.setGiveCouponCount(rule.getGiveCouponCount());
        return vo;
    }

    private int compareActivityDisplay(PcPromoActivityVo o1, PcPromoActivityVo o2) {
        if (!o1.getStatus().equals(o2.getStatus())) {
            return o2.getStatus().compareTo(o1.getStatus());
        }
        if (Long.valueOf(2L).equals(o1.getStatus())) {
            return o1.getEndTime().compareTo(o2.getEndTime());
        }
        return o1.getStartTime().compareTo(o2.getStartTime());
    }

    private <T> TableDataInfo<T> slicePage(List<T> allList, int pageNum, int pageSize) {
        int total = allList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            return new TableDataInfo<>(Collections.emptyList(), total);
        }
        int toIndex = Math.min(fromIndex + pageSize, total);
        return new TableDataInfo<>(allList.subList(fromIndex, toIndex), total);
    }

    /**
     * 判断当前用户能不能看见这条活动/券/满赠
     */
    private boolean checkUserEligibility(Long userScopeType, Long userParticipateType, ScopeBiz biz,
                                         Long bizId, Long userId, Long userLevelId,
                                         String tenant, String channel) {
        // 全员直接可见
        if (userScopeType == null || Long.valueOf(0L).equals(userScopeType)) {
            return true;
        }
        int participateType = userParticipateType == null ? 1 : userParticipateType.intValue();
        String modeTag = PmCacheKeys.modeTag((long) participateType);
        if (Long.valueOf(1L).equals(userScopeType)) {
            if (userId == null) {
                return participateType == 2;
            }
            String key = switch (biz) {
                case PROMO -> PmCacheKeys.promoUser(tenant, channel, modeTag, bizId);
                case GRANT -> PmCacheKeys.couponGrantUser(tenant, channel, modeTag, bizId);
                case COUPON -> PmCacheKeys.couponUser(tenant, channel, modeTag, bizId);
            };
            boolean hit = stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userId));
            return (participateType == 1) == hit;
        }
        if (Long.valueOf(2L).equals(userScopeType)) {
            if (userLevelId == null) {
                return participateType == 2;
            }
            String key = switch (biz) {
                case PROMO -> PmCacheKeys.promoLevel(tenant, channel, modeTag, bizId);
                case GRANT -> PmCacheKeys.couponGrantLevel(tenant, channel, modeTag, bizId);
                case COUPON -> PmCacheKeys.couponLevel(tenant, channel, modeTag, bizId);
            };
            boolean hit = stringRedisTemplate.opsForSet().isMember(key, String.valueOf(userLevelId));
            return (participateType == 1) == hit;
        }
        return true;
    }

    private enum ScopeBiz {
        PROMO, GRANT, COUPON
    }

    @Override
    public TableDataInfo<PcPromoGoodsVo> queryPromoGoodsList(PcPromoGoodsQueryBo query) {
        Long activityId = query.getActivityId();
        String tenant = TenantHelper.getTenantId();
        String channel = query.getChannelCode();
        Date now = new Date();
        PromoActivity activity = JsonUtils.parseObject(
            stringRedisTemplate.opsForValue().get(PmCacheKeys.promoDetail(tenant, channel, activityId)),
            PromoActivity.class);
        if (activity == null || now.after(activity.getEndTime())) {
            return TableDataInfo.build();
        }
        String promoType = activity.getPromoType();
        boolean isPriceChange = TYPE_SECKILL.equalsIgnoreCase(promoType) || TYPE_SPECIAL.equalsIgnoreCase(promoType);
        RemoteGoodsSkuSearchBo searchBo = new RemoteGoodsSkuSearchBo();
        searchBo.setTenantId(tenant);
        searchBo.setPageNum(query.getPageNum());
        searchBo.setPageSize(query.getPageSize());
        searchBo.setSortType(query.getSortType());
        if (isPriceChange) {
            // 秒杀/特价：ES promo_* 召回；价序走 promo_price（见 searchSkuForPromo）
            searchBo.setChannelCode(channel);
            searchBo.setPromoType(promoType);
            searchBo.setPromoActivityId(activityId);
            if (activity.getStatus() != null) {
                searchBo.setPromoStatus(activity.getStatus().intValue());
            }
        } else {
            boolean isCross = TYPE_CROSS_REDUCE.equalsIgnoreCase(promoType)
                || TYPE_CROSS_DISCOUNT.equalsIgnoreCase(promoType);
            if (!isCross) {
                String memberCode = activity.getMemberCode();
                if (MEMBER_CODE_PLATFORM.equalsIgnoreCase(memberCode)) {
                    searchBo.setGoodsClasses(List.of("plat", "supplier"));
                } else {
                    searchBo.setGoodsClasses(List.of("merchant"));
                    searchBo.setMemberCode(memberCode);
                }
            }
            if (Long.valueOf(1L).equals(activity.getScopeSupplierType())) {
                Set<String> codes = stringRedisTemplate.opsForSet()
                    .members(PmCacheKeys.promoTarget(tenant, channel, "supplier", activityId));
                if (CollUtil.isNotEmpty(codes)) {
                    searchBo.setMemberCcodes(codes.stream()
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .distinct()
                        .toList());
                }
            }
            applyItemScope(activity, searchBo, tenant, channel);
        }
        RemoteGoodsSkuSearchResult searchResult = searchSku(searchBo, activityId);
        List<RemoteGoodsSkuView> rows = searchResult.getRows();
        Map<Long, PromoGoodsSku> configMap = new HashMap<>();
        Map<Long, Long> stockMap = new HashMap<>();
        if (isPriceChange) {
            fillPriceChangeMaps(promoType, activityId, rows, configMap, stockMap, tenant, channel);
        }
        List<PcPromoGoodsVo> voList = new ArrayList<>(rows.size());
        for (RemoteGoodsSkuView sku : rows) {
            voList.add(buildVo(sku, isPriceChange, configMap, stockMap));
        }
        return new TableDataInfo<>(voList, searchResult.getTotal());
    }

    private RemoteGoodsSkuSearchResult searchSku(RemoteGoodsSkuSearchBo searchBo, Long activityId) {
        try {
            return remoteGoodsService.searchSkuForPromo(searchBo);
        } catch (Exception e) {
            log.error("活动商品 ES 检索失败, activityId={}", activityId, e);
            RemoteGoodsSkuSearchResult empty = new RemoteGoodsSkuSearchResult();
            empty.setTotal(0L);
            empty.setRows(List.of());
            return empty;
        }
    }


    /**
     * 给 ES 查询补上活动的商品范围
     */
    private void applyItemScope(PromoActivity activity, RemoteGoodsSkuSearchBo searchBo, String tenant, String channel) {
        Long scopeItemType = activity.getScopeItemType();
        if (Long.valueOf(0L).equals(scopeItemType)) {
            return;
        }
        String dim = "sku";
        if (Long.valueOf(2L).equals(scopeItemType)) {
            dim = "category";
        } else if (Long.valueOf(3L).equals(scopeItemType)) {
            dim = "brand";
        }
        String modeTag = PmCacheKeys.modeTag(activity.getItemParticipateType());
        List<Long> ids = parseLongIds(stringRedisTemplate.opsForSet()
            .members(PmCacheKeys.promoTarget(tenant, channel, dim, modeTag, activity.getId())));
        boolean black = "black".equals(modeTag);
        if ("sku".equals(dim)) {
            if (black) {
                searchBo.setExcludeSkuIds(ids);
            } else {
                searchBo.setSkuIds(ids);
            }
        } else if ("category".equals(dim)) {
            if (black) {
                searchBo.setExcludeCategoryBackendIds(ids);
            } else {
                searchBo.setCategoryBackendIds(ids);
            }
        } else {
            if (black) {
                searchBo.setExcludeBrandIds(ids);
            } else {
                searchBo.setBrandIds(ids);
            }
        }
    }

    /**
     * 给当前页的秒杀/特价 SKU，一次性从 Redis 把「活动配置」和「剩余库存」灌进两个 Map
     * @param configMap  skuId → 活动 SKU 配置
     * @param stockMap   skuId → 活动剩余库存
     */
    private void fillPriceChangeMaps(String promoType, Long activityId, List<RemoteGoodsSkuView> rows,
                                     Map<Long, PromoGoodsSku> configMap, Map<Long, Long> stockMap, String tenant, String channel) {
        boolean special = TYPE_SPECIAL.equalsIgnoreCase(promoType);
        List<String> configKeys = new ArrayList<>(rows.size());
        List<String> stockKeys = new ArrayList<>(rows.size());
        for (RemoteGoodsSkuView row : rows) {
            Long skuId = row.getSkuId();
            configKeys.add(special
                ? PmCacheKeys.specialSku(tenant, channel, activityId, skuId)
                : PmCacheKeys.seckillSku(tenant, channel, activityId, skuId));
            stockKeys.add(special
                ? PmCacheKeys.specialStock(tenant, channel, activityId, skuId)
                : PmCacheKeys.seckillStock(tenant, channel, activityId, skuId));
        }
        // 一次性获取所有商品，以及剩余库存
        List<String> configJsons = stringRedisTemplate.opsForValue().multiGet(configKeys);
        List<String> stocks = stringRedisTemplate.opsForValue().multiGet(stockKeys);
        for (int i = 0; i < rows.size(); i++) {
            Long skuId = rows.get(i).getSkuId();
            if (CollUtil.isNotEmpty(configJsons) && StringUtils.isNotBlank(configJsons.get(i))) {
                configMap.put(skuId, JsonUtils.parseObject(configJsons.get(i), PromoGoodsSku.class));
            }
            if (CollUtil.isNotEmpty(stocks) && StringUtils.isNotBlank(stocks.get(i))) {
                stockMap.put(skuId, Long.valueOf(stocks.get(i).trim()));
            }
        }
    }

    private PcPromoGoodsVo buildVo(RemoteGoodsSkuView sku, boolean isPriceChange,
                                   Map<Long, PromoGoodsSku> configMap, Map<Long, Long> stockMap) {
        PcPromoGoodsVo vo = new PcPromoGoodsVo();
        copyGoodsFields(vo, sku);
        if (!isPriceChange) {
            vo.setUserLimitQty(-1L);
            return vo;
        }
        PromoGoodsSku cfg = configMap.get(sku.getSkuId());
        if (cfg != null && cfg.getPromoPrice() != null) {
            vo.setPriceSales(cfg.getPromoPrice());
            vo.setUserLimitQty(cfg.getUserLimitQty());
            if (Long.valueOf(-1L).equals(cfg.getActivityStock())) {
                vo.setStock(sku.getStock());
            } else {
                Long left = stockMap.get(sku.getSkuId());
                if (left != null) {
                    vo.setStock(left);
                }
            }
        } else if (sku.getPromoPrice() != null) {
            // Redis 配置缺失时，展示价回退 ES 投影
            vo.setPriceSales(sku.getPromoPrice());
            vo.setUserLimitQty(-1L);
        }
        return vo;
    }

    private void copyGoodsFields(PcPromoGoodsVo vo, RemoteGoodsSkuView sku) {
        vo.setSkuId(sku.getSkuId());
        vo.setSpuId(sku.getSpuId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setSpuCode(sku.getSpuCode());
        vo.setTitle(sku.getTitle());
        vo.setSpecText(sku.getSpecText());
        vo.setCoverImage(sku.getCoverImage());
        vo.setPriceSales(sku.getPriceSales());
        vo.setPriceMarket(sku.getPriceMarket());
        vo.setStock(sku.getStock());
        vo.setSalesVolume(sku.getSalesVolume());
        vo.setWeight(sku.getWeight());
        vo.setCategoryBackendId(sku.getCategoryBackendId());
        vo.setCategoryFrontendId(sku.getCategoryFrontendId());
        vo.setBrandId(sku.getBrandId());
        vo.setBrandName(sku.getBrandName());
        vo.setGoodsClass(sku.getGoodsClass());
        vo.setMemberCode(sku.getMemberCode());
        vo.setMemberCcode(sku.getMemberCcode());
    }

    private List<Long> parseLongIds(Set<String> idStrs) {
        if (CollUtil.isEmpty(idStrs)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String s : idStrs) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            try {
                ids.add(Long.valueOf(s.trim()));
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return ids;
    }


    @Override
    public TableDataInfo<PcSeckillZoneGoodsVo> querySeckillZoneGoodsList(PcSeckillZoneQueryBo query) {
        String tenant = TenantHelper.getTenantId();
        String channel = query.getChannelCode();
        // 专区：ES 按渠道 + 秒杀进行中投影分页（价序用 promo_price）
        RemoteGoodsSkuSearchBo searchBo = new RemoteGoodsSkuSearchBo();
        searchBo.setTenantId(tenant);
        searchBo.setChannelCode(channel);
        searchBo.setPromoType(TYPE_SECKILL);
        searchBo.setPromoStatus(2);
        searchBo.setPageNum(query.getPageNum());
        searchBo.setPageSize(query.getPageSize());
        searchBo.setSortType(query.getSortType());
        RemoteGoodsSkuSearchResult searchResult = searchSku(searchBo, null);
        List<RemoteGoodsSkuView> skuList = searchResult.getRows();
        if (CollUtil.isEmpty(skuList)) {
            return new TableDataInfo<>(List.of(), searchResult.getTotal());
        }
        Set<Long> activityIds = skuList.stream()
            .map(RemoteGoodsSkuView::getPromoActivityId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, PromoActivity> activityMap = loadPromoDetails(tenant, channel, activityIds);
        List<String> configKeys = new ArrayList<>(skuList.size());
        List<String> stockKeys = new ArrayList<>(skuList.size());
        for (RemoteGoodsSkuView sku : skuList) {
            Long activityId = sku.getPromoActivityId();
            configKeys.add(PmCacheKeys.seckillSku(tenant, channel, activityId, sku.getSkuId()));
            stockKeys.add(PmCacheKeys.seckillStock(tenant, channel, activityId, sku.getSkuId()));
        }
        List<String> configJsons = stringRedisTemplate.opsForValue().multiGet(configKeys);
        List<String> stockValues = stringRedisTemplate.opsForValue().multiGet(stockKeys);
        List<PcSeckillZoneGoodsVo> voList = new ArrayList<>(skuList.size());
        for (int i = 0; i < skuList.size(); i++) {
            RemoteGoodsSkuView sku = skuList.get(i);
            Long activityId = sku.getPromoActivityId();
            PromoActivity activity = activityMap.get(activityId);
            if (activity == null) {
                continue;
            }
            PromoGoodsSku cfg = JsonUtils.parseObject(configJsons.get(i), PromoGoodsSku.class);
            PcSeckillZoneGoodsVo vo = new PcSeckillZoneGoodsVo();
            copyGoodsFields(vo, sku);
            vo.setActivityId(activityId);
            vo.setActivityTitle(activity.getTitle());
            vo.setStartTime(activity.getStartTime());
            vo.setEndTime(activity.getEndTime());
            if (cfg != null && cfg.getPromoPrice() != null) {
                vo.setPriceSales(cfg.getPromoPrice());
                vo.setUserLimitQty(cfg.getUserLimitQty());
                if (Long.valueOf(-1L).equals(cfg.getActivityStock())) {
                    vo.setStock(sku.getStock());
                } else if (StringUtils.isNotBlank(stockValues.get(i))) {
                    vo.setStock(Long.valueOf(stockValues.get(i)));
                }
            } else if (sku.getPromoPrice() != null) {
                vo.setPriceSales(sku.getPromoPrice());
            }
            voList.add(vo);
        }
        return new TableDataInfo<>(voList, searchResult.getTotal());
    }

    private Map<Long, PromoActivity> loadPromoDetails(String tenant, String channel, Set<Long> activityIds) {
        if (CollUtil.isEmpty(activityIds)) {
            return Map.of();
        }
        List<String> detailKeys = activityIds.stream()
            .map(id -> PmCacheKeys.promoDetail(tenant, channel, id))
            .collect(Collectors.toList());
        Map<Long, PromoActivity> result = new HashMap<>();
        for (String json : CollUtil.emptyIfNull(stringRedisTemplate.opsForValue().multiGet(detailKeys))) {
            PromoActivity activity = JsonUtils.parseObject(json, PromoActivity.class);
            if (activity != null && activity.getId() != null) {
                result.put(activity.getId(), activity);
            }
        }
        return result;
    }


    /**
     * 根据当前 SKU，从 Redis 里筛出「能展示的促销 / 满赠规则 / 优惠券」
     */
    @Override
    public PcPdpMarketingVo queryPdpMarketingInfo(PcPdpMarketingQueryBo query) {
        PcPdpMarketingVo vo = new PcPdpMarketingVo();
        String tenant = TenantHelper.getTenantId();
        String channel = query.getChannelCode();
        Long userId = null;
        Long userLevelId = null;
        if (LoginHelper.isLogin()) {
            userId = LoginHelper.getUserId();
            userLevelId = LoginHelper.getDistLevelId();
        }
        List<RemoteMasterSkuBo> goodsList = remoteGoodsService.selectBySkuIds(tenant, List.of(String.valueOf(query.getSkuId())));
        if (CollUtil.isEmpty(goodsList) || goodsList.get(0) == null) {
            throw new ServiceException("商品不存在");
        }
        RemoteMasterSkuBo goods = goodsList.get(0);
        query.setCategoryBackendId(goods.getCategoryBackendId());
        query.setBrandId(goods.getBrandId());
        query.setMemberCcode(goods.getMemberCcode());

        Date now = new Date();
        Set<String> promoIds = new HashSet<>();
        addSetMembers(promoIds, PmCacheKeys.promoScopeAll(tenant, channel));
        addSetMembers(promoIds, PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_SKU, query.getSkuId()));
        addSetMembers(promoIds, PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_CATEGORY, query.getCategoryBackendId()));
        addSetMembers(promoIds, PmCacheKeys.promoScope(tenant, channel, PmCacheKeys.CAT_BRAND, query.getBrandId()));
        List<PcPromoActivityVo> promoList = new ArrayList<>();
        List<String> promoIdList = new ArrayList<>(promoIds);
        List<String> promoDetailJsons = stringRedisTemplate.opsForValue().multiGet(
            promoIdList.stream().map(id -> PmCacheKeys.promoDetail(tenant, channel, Long.valueOf(id))).collect(Collectors.toList()));
        for (int i = 0; i < promoIdList.size(); i++) {
            String json = promoDetailJsons.get(i);
            if (StringUtils.isBlank(json)) {
                continue;
            }
            PromoActivity activity = JsonUtils.parseObject(json, PromoActivity.class);
            String promoType = activity.getPromoType();
            if (TYPE_SECKILL.equals(promoType) || TYPE_SPECIAL.equals(promoType)) {
                continue;
            }
            // 必须在活动起止时间内
            if (!inTimeWindow(activity.getStartTime(), activity.getEndTime(), now)) {
                continue;
            }
            // 与商品物权对齐（平台活动只挂平台/供应商货，商家活动仅同店）
            if (!StringUtils.equals(activity.getMemberCode(), goods.getMemberCode())) {
                continue;
            }
            if (!checkUserEligibility(activity.getUserScopeType(), activity.getUserParticipateType(),
                ScopeBiz.PROMO, activity.getId(), userId, userLevelId, tenant, channel)) {
                continue;
            }
            // 指定供应商活动，当前商品供应商不匹配则跳过
            if (Long.valueOf(1L).equals(activity.getScopeSupplierType())) {
                if (StringUtils.isBlank(query.getMemberCcode())
                    || Boolean.FALSE.equals(stringRedisTemplate.opsForSet().isMember(
                    PmCacheKeys.promoTarget(tenant, channel, "supplier", activity.getId()),
                    query.getMemberCcode()))) {
                    continue;
                }
            }
            // 再按 SKU / 叶子类目 / 品牌查黑名单 Set，命中则排除
            if (Long.valueOf(2L).equals(activity.getItemParticipateType())) {
                Long scopeItemType = activity.getScopeItemType();
                Long actId = activity.getId();
                Boolean hitBlack = false;
                if (Long.valueOf(1L).equals(scopeItemType)) {
                    hitBlack = stringRedisTemplate.opsForSet().isMember(
                        PmCacheKeys.promoTarget(tenant, channel, "sku", "black", actId),
                        String.valueOf(query.getSkuId()));
                } else if (Long.valueOf(2L).equals(scopeItemType)) {
                    hitBlack = stringRedisTemplate.opsForSet().isMember(
                        PmCacheKeys.promoTarget(tenant, channel, "category", "black", actId),
                        String.valueOf(query.getCategoryBackendId()));
                } else if (Long.valueOf(3L).equals(scopeItemType)) {
                    hitBlack = stringRedisTemplate.opsForSet().isMember(
                        PmCacheKeys.promoTarget(tenant, channel, "brand", "black", actId),
                        String.valueOf(query.getBrandId()));
                }
                if (Boolean.TRUE.equals(hitBlack)) {
                    continue;
                }
            }
            PcPromoActivityVo item = new PcPromoActivityVo();
            item.setId(activity.getId());
            item.setPromoType(promoType);
            item.setTitle(activity.getTitle());
            item.setCalcType(activity.getCalcType());
            item.setIsCap(activity.getIsCap());
            item.setStartTime(activity.getStartTime());
            item.setEndTime(activity.getEndTime());
            item.setImgUrl(activity.getImgUrl());
            item.setMemberCode(activity.getMemberCode());
            item.setMemberName(activity.getMemberName());
            item.setScopeItemType(activity.getScopeItemType());
            item.setChannelCode(activity.getChannelCode());
            fillPromoTierRules(item, activity.getId(), tenant, channel);
            promoList.add(item);
        }
        vo.setPromoList(promoList);

        List<CouponGiftGrantRuleVo> giftGrantList = new ArrayList<>();
        // 当前渠道进行中的满赠规则 ID
        Set<String> grantIds = stringRedisTemplate.opsForSet().members(PmCacheKeys.couponGrantActiveIds(tenant, channel));
        List<String> grantIdList = grantIds == null ? List.of() : new ArrayList<>(grantIds);
        List<String> grantDetailJsons = stringRedisTemplate.opsForValue().multiGet(
            grantIdList.stream().map(id -> PmCacheKeys.couponGrantDetail(tenant, channel, Long.valueOf(id))).collect(Collectors.toList()));
        for (int i = 0; i < grantIdList.size(); i++) {
            String json = grantDetailJsons.get(i);
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponGiftGrantRule rule = JsonUtils.parseObject(json, CouponGiftGrantRule.class);
            if (!inTimeWindow(rule.getStartTime(), rule.getEndTime(), now)) {
                continue;
            }
            // 与商品物权对齐：平台满赠只挂在平台/供应商商品（其 memberCode 为平台 code），不进商家店
            if (!StringUtils.equals(rule.getMemberCode(), goods.getMemberCode())) {
                continue;
            }
            if (!checkUserEligibility(rule.getUserScopeType(), rule.getUserParticipateType(),
                ScopeBiz.GRANT, rule.getId(), userId, userLevelId, tenant, channel)) {
                continue;
            }
            CouponGiftGrantRuleVo item = new CouponGiftGrantRuleVo();
            item.setId(rule.getId());
            item.setTitle(rule.getTitle());
            item.setChannelCode(rule.getChannelCode());
            item.setMemberCode(rule.getMemberCode());
            item.setMemberName(rule.getMemberName());
            item.setMinOrderAmount(rule.getMinOrderAmount());
            item.setGiveCouponCount(rule.getGiveCouponCount());
            item.setCouponTemplateId(rule.getCouponTemplateId());
            item.setStartTime(rule.getStartTime());
            item.setEndTime(rule.getEndTime());
            item.setImgUrl(rule.getImgUrl());
            giftGrantList.add(item);
        }
        vo.setGiftGrantList(giftGrantList);

        Set<String> couponIds = new HashSet<>();
        addSetMembers(couponIds, PmCacheKeys.couponScopeAll(tenant, channel));
        addSetMembers(couponIds, PmCacheKeys.couponScope(tenant, channel, PmCacheKeys.CAT_SKU, query.getSkuId()));
        addSetMembers(couponIds, PmCacheKeys.couponScope(tenant, channel, PmCacheKeys.CAT_CATEGORY, query.getCategoryBackendId()));
        addSetMembers(couponIds, PmCacheKeys.couponScope(tenant, channel, PmCacheKeys.CAT_BRAND, query.getBrandId()));
        List<String> couponIdList = new ArrayList<>(couponIds);
        List<String> couponDetailJsons = stringRedisTemplate.opsForValue().multiGet(
            couponIdList.stream().map(id -> PmCacheKeys.couponDetail(tenant, channel, Long.valueOf(id))).collect(Collectors.toList()));
        List<CouponTemplate> visibleCoupons = new ArrayList<>();
        for (int i = 0; i < couponIdList.size(); i++) {
            String json = couponDetailJsons.get(i);
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponTemplate template = JsonUtils.parseObject(json, CouponTemplate.class);
            if (template.getClaimStartTime().after(now) || !template.getClaimEndTime().after(now)) {
                continue;
            }
            if (!checkUserEligibility(template.getUserScopeType(), template.getUserParticipateType(),
                ScopeBiz.COUPON, template.getId(), userId, userLevelId, tenant, channel)) {
                continue;
            }
            if (Long.valueOf(1L).equals(template.getScopeSupplierType())) {
                if (StringUtils.isBlank(query.getMemberCcode())
                    || Boolean.FALSE.equals(stringRedisTemplate.opsForSet().isMember(
                    PmCacheKeys.couponTarget(tenant, channel, "supplier", template.getId()),
                    query.getMemberCcode()))) {
                    continue;
                }
            }
            if (Long.valueOf(2L).equals(template.getItemParticipateType())) {
                Long scopeItemType = template.getScopeItemType();
                Long templateId = template.getId();
                Boolean hitBlack = false;
                if (Long.valueOf(1L).equals(scopeItemType)) {
                    hitBlack = stringRedisTemplate.opsForSet().isMember(
                        PmCacheKeys.couponTarget(tenant, channel, "sku", "black", templateId),
                        String.valueOf(query.getSkuId()));
                } else if (Long.valueOf(2L).equals(scopeItemType)) {
                    hitBlack = stringRedisTemplate.opsForSet().isMember(
                        PmCacheKeys.couponTarget(tenant, channel, "category", "black", templateId),
                        String.valueOf(query.getCategoryBackendId()));
                } else if (Long.valueOf(3L).equals(scopeItemType)) {
                    hitBlack = stringRedisTemplate.opsForSet().isMember(
                        PmCacheKeys.couponTarget(tenant, channel, "brand", "black", templateId),
                        String.valueOf(query.getBrandId()));
                }
                if (Boolean.TRUE.equals(hitBlack)) {
                    continue;
                }
            }
            visibleCoupons.add(template);
        }
        List<CouponTemplateVo> couponList = new ArrayList<>(visibleCoupons.size());
        if (CollUtil.isNotEmpty(visibleCoupons)) {
            List<String> stockKeys = visibleCoupons.stream()
                .map(t -> PmCacheKeys.couponStock(tenant, channel, t.getId()))
                .collect(Collectors.toList());
            List<String> stockValues = stringRedisTemplate.opsForValue().multiGet(stockKeys);
            Map<Long, Long> stockMap = new HashMap<>();
            for (int i = 0; i < visibleCoupons.size(); i++) {
                String stockStr = stockValues.get(i);
                if (StringUtils.isBlank(stockStr)) {
                    continue;
                }
                stockMap.put(visibleCoupons.get(i).getId(), Long.valueOf(stockStr.trim()));
            }
            for (CouponTemplate template : visibleCoupons) {
                couponList.add(buildCouponCenterVo(template, userId, stockMap.get(template.getId()), tenant, channel));
            }
        }
        vo.setCouponList(couponList);
        return vo;
    }

    private void addSetMembers(Set<String> target, String key) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members != null) {
            target.addAll(members);
        }
    }

    private boolean inTimeWindow(Date start, Date end, Date now) {
        return !start.after(now) && end.after(now);
    }



    @Override
    public TableDataInfo<CouponTemplateVo> queryCouponCenterList(PcCouponCenterQueryBo query) {
        String tenant = TenantHelper.getTenantId();
        String channel = query.getChannelCode();
        Long userId = null;
        Long userLevelId = null;
        if (LoginHelper.isLogin()) {
            userId = LoginHelper.getUserId();
            userLevelId = LoginHelper.getDistLevelId();
        }
        // 拉取活跃券ID 集合
        Set<String> idStrs = stringRedisTemplate.opsForSet().members(PmCacheKeys.couponActiveIds(tenant, channel));
        if (CollUtil.isEmpty(idStrs)) {
            return TableDataInfo.build();
        }
        Date now = new Date();
        List<String> detailKeys = idStrs.stream()
            .map(id -> PmCacheKeys.couponDetail(tenant, channel, Long.valueOf(id)))
            .collect(Collectors.toList());
        List<CouponTemplate> visible = new ArrayList<>();
        for (String json : CollUtil.emptyIfNull(stringRedisTemplate.opsForValue().multiGet(detailKeys))) {
            if (StringUtils.isBlank(json)) {
                continue;
            }
            CouponTemplate template = JsonUtils.parseObject(json, CouponTemplate.class);
            if (!GRANT_NORMAL.equals(template.getGrantType())) {
                continue;
            }
            if (!inTimeWindow(template.getClaimStartTime(), template.getClaimEndTime(), now)) {
                continue;
            }
            if (!checkUserEligibility(template.getUserScopeType(), template.getUserParticipateType(),
                ScopeBiz.COUPON, template.getId(), userId, userLevelId, tenant, channel)) {
                continue;
            }
            visible.add(template);
        }
        // 按模板 id 倒序：新券靠前。
        visible.sort(Comparator.comparing(CouponTemplate::getId).reversed());
        int total = visible.size();
        int fromIndex = (query.getPageNum() - 1) * query.getPageSize();
        if (fromIndex >= total) {
            return new TableDataInfo<>(List.of(), total);
        }
        List<CouponTemplate> pageList = visible.subList(fromIndex, Math.min(fromIndex + query.getPageSize(), total));
        List<String> stockKeys = pageList.stream()
            .map(t -> PmCacheKeys.couponStock(tenant, channel, t.getId()))
            .collect(Collectors.toList());
        List<String> stockValues = stringRedisTemplate.opsForValue().multiGet(stockKeys);
        Map<Long, Long> stockMap = new HashMap<>();
        for (int i = 0; i < pageList.size(); i++) {
            String stockStr = stockValues.get(i);
            if (StringUtils.isBlank(stockStr)) {
                continue;
            }
            stockMap.put(pageList.get(i).getId(), Long.valueOf(stockStr.trim()));
        }
        List<CouponTemplateVo> voList = new ArrayList<>(pageList.size());
        for (CouponTemplate template : pageList) {
            voList.add(buildCouponCenterVo(template, userId, stockMap.get(template.getId()), tenant, channel));
        }
        return new TableDataInfo<>(voList, total);
    }

    private CouponTemplateVo buildCouponCenterVo(CouponTemplate template, Long userId,
                                                 Long remainStock, String tenant, String channel) {
        CouponTemplateVo vo = MapstructUtils.convert(template, CouponTemplateVo.class);
        vo.setRemainStock(remainStock);
        Long userLimitCount = template.getUserLimitCount();
        vo.setUserLimitCount(userLimitCount);
        long userClaimedQty = 0L;
        if (userId != null && userLimitCount != null && userLimitCount > 0) {
            Object claimed = stringRedisTemplate.opsForHash()
                .get(PmCacheKeys.couponUserLimit(tenant, channel, template.getId()), String.valueOf(userId));
            if (claimed != null) {
                userClaimedQty = Long.parseLong(claimed.toString());
            }
        }
        vo.setUserClaimedQty(userClaimedQty);
        return vo;
    }

    @Override
    public TableDataInfo<CouponUserRelVo> queryMyCouponList(PcMyCouponQueryBo query) {
        if (!LoginHelper.isLogin()) {
            throw new ServiceException("请先登录");
        }
        Long userId = LoginHelper.getUserId();
        Date now = new Date();
        int statusType = query.getStatusType();
        LambdaQueryWrapper<CouponUserRel> lqw = Wrappers.lambdaQuery();
        lqw.eq(CouponUserRel::getUserId, userId);
        lqw.eq(CouponUserRel::getChannelCode, query.getChannelCode());
        switch (statusType) {
            // 未使用且还没过使用截止时间
            case 0 -> lqw.eq(CouponUserRel::getStatus, USER_COUPON_UNUSED).ge(CouponUserRel::getEndTime, now);
            case 1 -> lqw.eq(CouponUserRel::getStatus, USER_COUPON_USED);
            // 已过期或者未使用并且过了使用截止时间
            case 2 -> lqw.and(w -> w.eq(CouponUserRel::getStatus, USER_COUPON_EXPIRED)
                .or(x -> x.eq(CouponUserRel::getStatus, USER_COUPON_UNUSED)
                    .lt(CouponUserRel::getEndTime, now)));
            case 3 -> lqw.eq(CouponUserRel::getStatus, USER_COUPON_VOIDED);
            default -> throw new ServiceException("状态类型不合法");
        }
        if (statusType == 0) {
            // 未使用，快过期的排前面
            lqw.orderByAsc(CouponUserRel::getEndTime).orderByAsc(CouponUserRel::getId);
        } else {
            lqw.orderByDesc(CouponUserRel::getId);
        }
        Page<CouponUserRel> page = couponUserRelMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), lqw);
        List<CouponUserRel> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new TableDataInfo<>(List.of(), page.getTotal());
        }
        List<CouponUserRelVo> voList = new ArrayList<>(records.size());
        for (CouponUserRel rel : records) {
            CouponUserRelVo vo = MapstructUtils.convert(rel, CouponUserRelVo.class);
            if (Long.valueOf(USER_COUPON_UNUSED).equals(rel.getStatus()) && rel.getEndTime().before(now)) {
                vo.setStatus(USER_COUPON_EXPIRED);
            }
            voList.add(vo);
        }
        return new TableDataInfo<>(voList, page.getTotal());
    }

    @Override
    public void claimCoupon(PcCouponClaimBo bo) {
        if (!LoginHelper.isLogin()) {
            throw new ServiceException("请先登录后再领取优惠券");
        }
        Long userId = LoginHelper.getUserId();
        Long userLevelId = LoginHelper.getDistLevelId();
        String tenant = TenantHelper.getTenantId();
        String channel = bo.getChannelCode();
        Long templateId = bo.getTemplateId();
        // 标记Lua已成功扣过
        boolean redisDeducted = false;
        CouponTemplate template = null;
        try {
            String detailJson = stringRedisTemplate.opsForValue().get(PmCacheKeys.couponDetail(tenant, channel, templateId));
            if (StringUtils.isBlank(detailJson)) {
                throw new ServiceException("优惠券已下线或不存在");
            }
            template = JsonUtils.parseObject(detailJson, CouponTemplate.class);
            long nowMillis = System.currentTimeMillis();
            // 距离领取结束还剩多少秒
            long remain = (template.getClaimEndTime().getTime() - nowMillis) / 1000L;
            long ttlSeconds = Math.max(remain + 3600L, 3600L);
            List<String> keys = Arrays.asList(
                PmCacheKeys.couponDetail(tenant, channel, templateId),
                PmCacheKeys.couponStock(tenant, channel, templateId),
                PmCacheKeys.couponUserLimit(tenant, channel, templateId),
                PmCacheKeys.couponUser(tenant, channel, "white", templateId),
                PmCacheKeys.couponUser(tenant, channel, "black", templateId),
                PmCacheKeys.couponLevel(tenant, channel, "white", templateId),
                PmCacheKeys.couponLevel(tenant, channel, "black", templateId)
            );
            Long luaResult = stringRedisTemplate.execute(
                couponClaimScript,
                keys,
                String.valueOf(userId),
                String.valueOf(nowMillis),
                userLevelId == null ? "" : String.valueOf(userLevelId),
                String.valueOf(template.getClaimStartTime().getTime()),
                String.valueOf(template.getClaimEndTime().getTime()),
                String.valueOf(ttlSeconds)
            );
            if (luaResult != 1L) {
                handleCouponClaimLuaError(luaResult);
            }
            redisDeducted = true;
            SpringUtils.getAopProxy(this).persistCouponClaim(template, userId);
        } catch (RuntimeException e) {
            if (redisDeducted) {
                compensateCouponClaimRedis(template, userId, tenant, channel);
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void persistCouponClaim(CouponTemplate template, Long userId) {
        Date[] window = CouponValidTimeUtils.resolveUserWindow(template, new Date());
        CouponUserRel rel = new CouponUserRel();
        rel.setId(IdGeneratorUtil.nextLongId());
        rel.setTenantId(template.getTenantId());
        rel.setUserId(userId);
        rel.setCouponTemplateId(template.getId());
        rel.setChannelCode(template.getChannelCode());
        rel.setTitle(template.getTitle());
        rel.setCouponType(template.getCouponType());
        rel.setImgUrl(template.getImgUrl());
        rel.setUserCouponCode(IdUtil.getSnowflakeNextIdStr());
        rel.setMinSpendAmount(template.getMinSpendAmount());
        rel.setBenefitValue(template.getBenefitValue());
        rel.setStartTime(window[0]);
        rel.setEndTime(window[1]);
        rel.setStatus(0L);
        couponUserRelMapper.insert(rel);
    }

    /**
     * Lua 占了名额但发券落库失败时，把名额还给 Redis。
     */
    private void compensateCouponClaimRedis(CouponTemplate template, Long userId, String tenant, String channel) {
        Long templateId = template.getId();
        try {
            stringRedisTemplate.opsForValue().increment(PmCacheKeys.couponStock(tenant, channel, templateId));
            Long userLimit = template.getUserLimitCount();
            if (userLimit != null && userLimit > 0) {
                stringRedisTemplate.opsForHash()
                    .increment(PmCacheKeys.couponUserLimit(tenant, channel, templateId), String.valueOf(userId), -1L);
            }
            log.warn("领券落库失败，已补偿 Redis，templateId={}, userId={}", templateId, userId);
        } catch (Exception ex) {
            log.error("领券 Redis 补偿失败，需人工核对 templateId={}, userId={}", templateId, userId, ex);
        }
    }

    private void handleCouponClaimLuaError(Long code) {
        if (code == null) {
            throw new ServiceException("领券失败，系统异常");
        }
        switch (code.intValue()) {
            case -1 -> throw new ServiceException("优惠券已下线或不存在");
            case -2 -> throw new ServiceException("领券活动尚未开始");
            case -3 -> throw new ServiceException("领券活动已结束");
            case -4 -> throw new ServiceException("抱歉，您暂不符合该券的领取资格");
            case -5 -> throw new ServiceException("您已达到该优惠券的最大领取上限");
            case -6 -> throw new ServiceException("手慢了，优惠券已被抢光啦");
            case -7 -> throw new ServiceException("该优惠券不支持手动领取");
            default -> throw new ServiceException("领券失败，请稍后重试");
        }
    }

}

