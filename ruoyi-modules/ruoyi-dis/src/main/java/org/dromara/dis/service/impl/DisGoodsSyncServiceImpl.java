package org.dromara.dis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.dis.api.constant.DisRedisConstants;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.dis.domain.Channel;
import org.dromara.dis.domain.Dgoods;
import org.dromara.dis.domain.PriceRule;
import org.dromara.dis.domain.StockRule;
import org.dromara.dis.domain.dto.DisGoodsEsAttrItem;
import org.dromara.dis.domain.dto.DisGoodsEsDTO;
import org.dromara.dis.engine.DisSyncEngine;
import org.dromara.dis.mapper.ChannelMapper;
import org.dromara.dis.mapper.DgoodsMapper;
import org.dromara.dis.service.IDisGoodsSyncService;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.goods.api.domain.RemoteGoodsAttrBo;
import org.dromara.goods.api.domain.RemoteMasterSkuBo;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisGoodsSyncServiceImpl implements IDisGoodsSyncService {

    private static final int BATCH_SIZE = 500;

    private final DgoodsMapper dgoodsMapper;
    private final ChannelMapper channelMapper;
    private final StringRedisTemplate redisTemplate;
    private final DisSyncEngine syncEngine;
    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    @Override
    public void startManualGoodsSync(String tenantId, String channelCode) {
        TenantHelper.dynamic(tenantId, () -> doManualSync(tenantId, channelCode));
    }

    private void doManualSync(String tenantId, String channelCode) {
        Dgoods cfg = requirePublishConfig(tenantId, channelCode);
        if (!on(cfg.getDgoodsType())) {
            throw new ServiceException("渠道未开启商品发布，无法手动同步");
        }
        String parentChannelCode = requireParentChannelCode(tenantId, channelCode);
        Long lastSkuId = 0L;
        while (true) {
            // 用游标分页从 goods 服务拉父渠道主站 SKU
            List<RemoteMasterSkuBo> page = remoteGoodsService.selectPageByCursor(tenantId, parentChannelCode, lastSkuId, BATCH_SIZE);
            if (CollUtil.isEmpty(page)) {
                break;
            }
            reconcileToChannel(tenantId, channelCode, cfg, page, false);
            lastSkuId = page.get(page.size() - 1).getSkuId();
        }
    }

    private String requireParentChannelCode(String tenantId, String channelCode) {
        Channel channel = channelMapper.selectOne(Wrappers.lambdaQuery(Channel.class)
            .eq(Channel::getTenantId, tenantId)
            .eq(Channel::getChannelCode, channelCode)
            .last("LIMIT 1"));
        if (channel == null) {
            throw new ServiceException("渠道不存在，无法手动同步");
        }
        if (StrUtil.isBlank(channel.getParentChannelCode())) {
            throw new ServiceException("渠道没有上级，无法从主站同步商品");
        }
        return channel.getParentChannelCode();
    }

    @Override
    public void syncSingleSpuToChannels(Long spuId, String tenantId, String parentChannelCode) {
        TenantHelper.dynamic(tenantId, () -> doSync(spuId, tenantId, parentChannelCode));
    }

    private void doSync(Long spuId, String tenantId, String parentChannelCode) {
        if (StrUtil.isBlank(parentChannelCode)) {
            log.warn("自动同步缺少父渠道, spuId={}, tenantId={}", spuId, tenantId);
            return;
        }
        List<String> childChannels = listChildChannelCodes(tenantId, parentChannelCode);
        if (CollUtil.isEmpty(childChannels)) {
            return;
        }
        List<RemoteMasterSkuBo> skuList = remoteGoodsService.selectSkuFullInfoBySpuId(tenantId, spuId);
        if (CollUtil.isEmpty(skuList)) {
            return;
        }
        for (String channelCode : childChannels) {
            Dgoods cfg = getPublishConfig(tenantId, channelCode);
            // 商品自动同步或者商品发布权限关闭则不同渠道
            if (cfg == null || !on(cfg.getDgoodsType()) || !on(cfg.getDgoodsSync())) {
                continue;
            }
            reconcileToChannel(tenantId, channelCode, cfg, skuList, true);
        }
    }


    /**
     * 是把一批主站 SKU，按渠道配置同步到指定渠道商品
     * @param respectEditFreeze  true强制全量同步
     */
    private void reconcileToChannel(String tenantId, String channelCode, Dgoods cfg, List<RemoteMasterSkuBo> masterSkus, boolean respectEditFreeze) {
        Map<Object, Object> termMap = redisTemplate.opsForHash().entries(
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_TERM, tenantId, channelCode));
        Set<String> skuScopeIds = redisTemplate.opsForSet().members(
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, "SKU"));
        Set<String> categoryScopeIds = redisTemplate.opsForSet().members(
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, "CATEGORY"));
        Set<String> brandScopeIds = redisTemplate.opsForSet().members(
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, "BRAND"));
        StockRule stockRule = topStockRule(tenantId, channelCode);
        List<RemoteMasterSkuBo> toOff = new ArrayList<>();
        List<RemoteMasterSkuBo> toSync = new ArrayList<>();
        for (RemoteMasterSkuBo sku : masterSkus) {
            boolean masterSellable = on(sku.getSkuSaleStatus());
            boolean inScope = matchScope(sku, termMap, skuScopeIds, categoryScopeIds, brandScopeIds, cfg.getProductNonAll());
            if (!masterSellable || !inScope) {
                toOff.add(sku);
                continue;
            }
            RemoteMasterSkuBo exist = remoteGoodsService.findChannelSku(tenantId, channelCode, sku.getSkuCode());
            // 自动同步时，如果渠道关掉了「修改自动更新」，且这条货渠道里已经有副本，就不要再改它。
            if (respectEditFreeze && !on(cfg.getDgoodsEdit()) && exist != null) {
                continue;
            }
            toSync.add(buildChannelSku(tenantId, sku, channelCode, cfg, stockRule, exist));
        }
        Map<Long, List<RemoteMasterSkuBo>> bySpu = toSync.stream()
            .collect(Collectors.groupingBy(RemoteMasterSkuBo::getSpuId));
        for (Map.Entry<Long, List<RemoteMasterSkuBo>> entry : bySpu.entrySet()) {
            Long masterSpuId = entry.getKey();
            List<RemoteMasterSkuBo> saved = remoteGoodsService.upsertChannelSpuGoods(tenantId, channelCode, entry.getValue());
            List<RemoteGoodsAttrBo> attrs = remoteGoodsService.selectAttrsBySpuId(tenantId, masterSpuId);
            for (RemoteMasterSkuBo item : saved) {
                indexEs(buildEsDto(item, tenantId, channelCode, attrs));
            }
        }
        if (!toOff.isEmpty()) {
            forceOff(tenantId, toOff, List.of(channelCode));
        }
    }

    private RemoteMasterSkuBo buildChannelSku(String tenantId, RemoteMasterSkuBo sku, String channelCode,
                                              Dgoods cfg, StockRule stockRule, RemoteMasterSkuBo exist) {
        PriceRule priceRule = topPriceRule(tenantId, channelCode, sku);
        // 开了接受上游价格或者渠道还没有副本用主站售价，否则用渠道已有售价
        BigDecimal basePrice = (on(cfg.getDgoodsUprice()) || exist == null)
            ? sku.getOriginPriceSales() : exist.getOriginPriceSales();
        BigDecimal finalPrice;
        BigDecimal marketPrice;
        // 已有副本 + 价格规则存在但价格自动更新关闭冻住渠道现价
        if (exist != null && priceRule != null && !on(priceRule.getDpriceSync())) {
            finalPrice = exist.getOriginPriceSales();
            marketPrice = exist.getOriginPriceMarket();
        } else {
            finalPrice = syncEngine.calculateSalesPrice(basePrice, priceRule);
            marketPrice = syncEngine.calculateMarketPrice(sku.getOriginPriceMarket(), priceRule);
        }
        // 开了接受上游库存或者渠道还没有副本用主站库存，否则用渠道已有库存
        Integer baseStock = (on(cfg.getDgoodsUnum()) || exist == null)
            ? sku.getOriginStock() : exist.getOriginStock();
        // 已有副本 + 库存规则关了自动改库存 → 冻住渠道库存
        Integer finalStock = (exist != null && stockRule != null && !on(stockRule.getDgnumSync()))
            ? exist.getOriginStock()
            : syncEngine.calculateStock(baseStock, stockRule);
        int safety = stockRule == null || stockRule.getDgnumMin() == null ? 0 : stockRule.getDgnumMin().intValue();
        // 库存高于安全库存 且 渠道开了自动上架则上架，否则下架
        int saleStatus = finalStock > safety && on(cfg.getDgoodsPut()) ? 1 : 0;

        RemoteMasterSkuBo toSave = BeanUtil.toBean(sku, RemoteMasterSkuBo.class);
        toSave.setOriginPriceSales(finalPrice);
        toSave.setOriginPriceMarket(marketPrice);
        toSave.setOriginStock(finalStock);
        toSave.setSaleStatus(saleStatus);
        return toSave;
    }

    /** 强制下架指定渠道副本并刷 ES */
    private void forceOff(String tenantId, List<RemoteMasterSkuBo> skuList, List<String> childChannels) {
        List<String> codes = skuList.stream().map(RemoteMasterSkuBo::getSkuCode).distinct().toList();
        for (RemoteMasterSkuBo item : remoteGoodsService.offSaleChannelBySourceSkuCodes(tenantId, codes, childChannels)) {
            try {
                Map<String, Object> doc = new HashMap<>(2);
                doc.put("sale_status", 0);
                doc.put("sku_sale_status", 0);
                restHighLevelClient.update(new UpdateRequest(DisGoodsMqConstants.ES_INDEX_DIS_GOODS,
                    item.getChannelCode() + "_" + item.getSkuId()).doc(doc), RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("强制下架更新 ES 失败, channel={}, skuId={}", item.getChannelCode(), item.getSkuId(), e);
            }
        }
    }

    @Override
    public void removeChannelGoodsBySourceSkuCodes(String tenantId, String parentChannelCode,
                                                   Collection<String> sourceSkuCodes) {
        TenantHelper.dynamic(tenantId, () -> doRemoveChannelGoods(tenantId, parentChannelCode, sourceSkuCodes));
    }

    private void doRemoveChannelGoods(String tenantId, String parentChannelCode, Collection<String> sourceSkuCodes) {
        if (StrUtil.isBlank(parentChannelCode) || CollUtil.isEmpty(sourceSkuCodes)) {
            return;
        }
        List<String> childChannels = listChildChannelCodes(tenantId, parentChannelCode);
        if (CollUtil.isEmpty(childChannels)) {
            return;
        }
        List<RemoteMasterSkuBo> remoteMasterSkuBos = remoteGoodsService.removeChannelGoodsBySourceSkuCodes(tenantId, sourceSkuCodes, childChannels);
        for (RemoteMasterSkuBo item :remoteMasterSkuBos) {
            try {
                restHighLevelClient.delete(new DeleteRequest(DisGoodsMqConstants.ES_INDEX_DIS_GOODS,
                    item.getChannelCode() + "_" + item.getSkuId()), RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("删除渠道商品 ES 失败, channel={}, skuId={}", item.getChannelCode(), item.getSkuId(), e);
            }
        }
    }

    private List<String> listChildChannelCodes(String tenantId, String parentChannelCode) {
        return channelMapper.selectList(Wrappers.lambdaQuery(Channel.class)
                .eq(Channel::getTenantId, tenantId)
                .eq(Channel::getParentChannelCode, parentChannelCode))
            .stream().map(Channel::getChannelCode).toList();
    }

    /**
     * 这条主站 SKU 是否落在当前渠道的商品选品范围内
     */
    private boolean matchScope(RemoteMasterSkuBo sku,
                               Map<Object, Object> termMap,
                               Set<String> skuScopeIds,
                               Set<String> categoryScopeIds,
                               Set<String> brandScopeIds,
                               Long productNonAll) {
        // 完全没配选品，并且开了接受上游规则，则直接全量
        if (CollUtil.isEmpty(termMap) && CollUtil.isEmpty(skuScopeIds) && CollUtil.isEmpty(categoryScopeIds) && CollUtil.isEmpty(brandScopeIds)) {
            return on(productNonAll);
        }
        // 需要同时满足，IN：值在集合里才通过  NOT_IN：值不在集合里才通过
        return matchDim(Convert.toStr(termMap.get("SKU")), skuScopeIds, Convert.toStr(sku.getSkuId()))
            && matchDim(Convert.toStr(termMap.get("CATEGORY")), categoryScopeIds, Convert.toStr(sku.getCategoryBackendId()))
            && matchDim(Convert.toStr(termMap.get("BRAND")), brandScopeIds, Convert.toStr(sku.getBrandId()));
    }

    private boolean matchDim(String term, Set<String> set, String val) {
        if (CollUtil.isEmpty(set)) {
            return true;
        }
        boolean inSet = set.contains(val);
        return "NOT_IN".equalsIgnoreCase(term) != inSet;
    }

    private Dgoods requirePublishConfig(String tenantId, String channelCode) {
        Dgoods cfg = getPublishConfig(tenantId, channelCode);
        if (cfg == null) {
            throw new ServiceException("未找到渠道发布设置，同步中断");
        }
        return cfg;
    }

    private Dgoods getPublishConfig(String tenantId, String channelCode) {
        String redisKey = String.format(DisRedisConstants.KEY_PUBLISH_CONFIG, tenantId, channelCode);
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(redisKey);
        if (cached.containsKey("dgoodsSync")) {
            Dgoods cfg = new Dgoods();
            cfg.setTenantId(tenantId);
            cfg.setChannelCode(channelCode);
            cfg.setDgoodsType(Convert.toLong(cached.get("dgoodsType")));
            cfg.setDgoodsSync(Convert.toLong(cached.get("dgoodsSync")));
            cfg.setDgoodsPut(Convert.toLong(cached.get("dgoodsPut")));
            cfg.setDgoodsUprice(Convert.toLong(cached.get("dgoodsUprice")));
            cfg.setDgoodsUnum(Convert.toLong(cached.get("dgoodsUnum")));
            cfg.setDgoodsEdit(Convert.toLong(cached.get("dgoodsEdit")));
            cfg.setProductNonAll(Convert.toLong(cached.get("productNonAll")));
            return cfg;
        }
        Dgoods fromDb = dgoodsMapper.selectOne(Wrappers.lambdaQuery(Dgoods.class)
            .eq(Dgoods::getTenantId, tenantId)
            .eq(Dgoods::getChannelCode, channelCode));
        if (fromDb != null) {
            Map<String, String> map = new HashMap<>(8);
            map.put("dgoodsType", String.valueOf(fromDb.getDgoodsType()));
            map.put("dgoodsSync", String.valueOf(fromDb.getDgoodsSync()));
            map.put("dgoodsPut", String.valueOf(fromDb.getDgoodsPut()));
            map.put("dgoodsUprice", String.valueOf(fromDb.getDgoodsUprice()));
            map.put("dgoodsUnum", String.valueOf(fromDb.getDgoodsUnum()));
            map.put("dgoodsEdit", String.valueOf(fromDb.getDgoodsEdit()));
            map.put("productNonAll", String.valueOf(fromDb.getProductNonAll()));
            redisTemplate.opsForHash().putAll(redisKey, map);
        }
        return fromDb;
    }

    private PriceRule topPriceRule(String tenantId, String channelCode, RemoteMasterSkuBo sku) {
        Set<String> ids = redisTemplate.opsForZSet().range(
            String.format(DisRedisConstants.KEY_PRICE_RULE_ZSET, tenantId, channelCode), 0, -1);
        if (CollUtil.isEmpty(ids)) {
            return null;
        }
        Date now = new Date();
        for (String id : ids) {
            String detail = redisTemplate.opsForValue().get(
                String.format(DisRedisConstants.KEY_PRICE_RULE_DETAIL, tenantId, id));
            if (StrUtil.isBlank(detail)) {
                continue;
            }
            PriceRule rule = JSON.parseObject(detail, PriceRule.class);
            if (timeOk(now, rule.getDpriceSydate(), rule.getDpriceEydate())
                && priceRuleHit(tenantId, rule.getId(), sku)) {
                return rule;
            }
        }
        return null;
    }

    private boolean priceRuleHit(String tenantId, Long ruleId, RemoteMasterSkuBo sku) {
        String id = String.valueOf(ruleId);
        Set<String> skuScope = redisTemplate.opsForSet().members(
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, id, "1"));
        Set<String> catScope = redisTemplate.opsForSet().members(
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, id, "2"));
        Set<String> brandScope = redisTemplate.opsForSet().members(
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, id, "3"));
        return (CollUtil.isEmpty(skuScope) || skuScope.contains(Convert.toStr(sku.getSkuId())))
            && (CollUtil.isEmpty(catScope) || catScope.contains(Convert.toStr(sku.getCategoryBackendId())))
            && (CollUtil.isEmpty(brandScope) || brandScope.contains(Convert.toStr(sku.getBrandId())));
    }

    private StockRule topStockRule(String tenantId, String channelCode) {
        Set<String> ids = redisTemplate.opsForZSet().range(
            String.format(DisRedisConstants.KEY_STOCK_RULE_ZSET, tenantId, channelCode), 0, -1);
        if (CollUtil.isEmpty(ids)) {
            return null;
        }
        List<String> details = redisTemplate.opsForValue().multiGet(ids.stream()
            .map(id -> String.format(DisRedisConstants.KEY_STOCK_RULE_DETAIL, tenantId, id))
            .toList());
        Date now = new Date();
        for (String detail : details) {
            if (StrUtil.isBlank(detail)) {
                continue;
            }
            StockRule rule = JSON.parseObject(detail, StockRule.class);
            if (timeOk(now, rule.getDgnumStydate(), rule.getDgnumEydate())) {
                return rule;
            }
        }
        return null;
    }

    private boolean timeOk(Date now, Date start, Date end) {
        return !now.before(start) && !now.after(end);
    }

    private DisGoodsEsDTO buildEsDto(RemoteMasterSkuBo sku, String tenantId, String channelCode,
                                     List<RemoteGoodsAttrBo> attrs) {
        DisGoodsEsDTO es = BeanUtil.toBean(sku, DisGoodsEsDTO.class);
        es.setDocId(channelCode + "_" + sku.getSkuId());
        es.setChannelCode(channelCode);
        es.setTenantId(tenantId);
        es.setSkuCode(StrUtil.blankToDefault(sku.getSkuNo(), sku.getSkuCode()));
        es.setSpuCode(StrUtil.blankToDefault(sku.getSpuNo(), sku.getSpuCode()));
        es.setPriceSales(sku.getOriginPriceSales());
        es.setPriceMarket(sku.getOriginPriceMarket());
        es.setStock(sku.getOriginStock());
        es.setSkuSaleStatus(sku.getSaleStatus());
        es.setSaleStatus(sku.getSaleStatus());
        es.setWeight(sku.getWeight().intValue());
        es.setSuggest(List.of(sku.getTitle()));
        es.setAttrs(attrs.stream().map(a -> {
            DisGoodsEsAttrItem item = new DisGoodsEsAttrItem();
            item.setAttrId(a.getAttrId());
            item.setAttrName(a.getAttrName());
            item.setOptionId(a.getOptionId());
            item.setOptionValue(a.getOptionValue());
            return item;
        }).toList());
        es.setDelFlag((byte) 0);
        return es;
    }

    private void indexEs(DisGoodsEsDTO esDoc) {
        try {
            restHighLevelClient.index(new IndexRequest(DisGoodsMqConstants.ES_INDEX_DIS_GOODS)
                .id(esDoc.getDocId())
                .source(objectMapper.writeValueAsString(esDoc), XContentType.JSON), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("写入 ES 失败: " + esDoc.getDocId(), e);
        }
    }

    private static boolean on(Number flag) {
        return flag != null && flag.longValue() == 1L;
    }
}
