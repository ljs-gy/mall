package org.dromara.dis.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dis.api.constant.DisRedisConstants;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.dis.domain.Dgoods;
import org.dromara.dis.domain.DgoodsBuy;
import org.dromara.dis.domain.DgoodsScopelist;
import org.dromara.dis.domain.PriceRule;
import org.dromara.dis.domain.StockRule;
import org.dromara.dis.domain.dto.DisGoodsPublishConfigSaveDTO;
import org.dromara.dis.domain.dto.DisGoodsScopeItem;
import org.dromara.dis.domain.dto.DisGoodsScopeSaveDTO;
import org.dromara.dis.domain.dto.DisUserScopeItem;
import org.dromara.dis.domain.dto.DisUserScopeSaveDTO;
import org.dromara.dis.domain.vo.DgoodsBuyVo;
import org.dromara.dis.domain.vo.DgoodsScopelistVo;
import org.dromara.dis.domain.vo.DgoodsVo;
import org.dromara.dis.mapper.DgoodsBuyMapper;
import org.dromara.dis.mapper.DgoodsMapper;
import org.dromara.dis.mapper.DgoodsScopelistMapper;
import org.dromara.dis.mapper.PriceRuleMapper;
import org.dromara.dis.mapper.PriceRuleScopeMapper;
import org.dromara.dis.mapper.StockRuleMapper;
import org.dromara.dis.service.IDisChannelConfigService;
import org.dromara.goods.api.RemoteGoodsService;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渠道用户范围 / 发布设置 / 商品选品（落库 + 刷 Redis）
 *
 * @author ljs
 * @date 2026-08-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisChannelConfigServiceImpl implements IDisChannelConfigService {

    private final DgoodsBuyMapper dgoodsBuyMapper;
    private final DgoodsMapper dgoodsMapper;
    private final DgoodsScopelistMapper dgoodsScopelistMapper;
    private final PriceRuleMapper priceRuleMapper;
    private final PriceRuleScopeMapper priceRuleScopeMapper;
    private final StockRuleMapper stockRuleMapper;
    private final StringRedisTemplate redisTemplate;
    private final RestHighLevelClient restHighLevelClient;

    @DubboReference
    private RemoteGoodsService remoteGoodsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveUserScope(DisUserScopeSaveDTO dto) {
        if (CollUtil.isEmpty(dto.getScopeItems())) {
            return;
        }
        String tenantId = LoginHelper.getTenantId();
        String channelCode = dto.getChannelCode();
        String termKey = String.format(DisRedisConstants.KEY_USER_SCOPE_TERM, tenantId, channelCode);
        List<DgoodsBuy> list = new ArrayList<>(dto.getScopeItems().size());
        Map<String, String> termMap = new HashMap<>();
        Map<String, List<String>> valueSetMap = new HashMap<>();
        for (DisUserScopeItem item : dto.getScopeItems()) {
            putScopeTerm(termMap, termKey, item.getBuyType(), item.getBuyTerm());
            DgoodsBuy buy = new DgoodsBuy();
            buy.setTenantId(tenantId);
            buy.setChannelCode(channelCode);
            buy.setDgoodsBuyType(item.getBuyType());
            buy.setDgoodsBuyTerm(item.getBuyTerm());
            buy.setDgoodsBuyValue(item.getBuyValue());
            buy.setRuleValueName(item.getRuleValueName());
            list.add(buy);
            valueSetMap.computeIfAbsent(item.getBuyType(), k -> new ArrayList<>()).add(item.getBuyValue());
        }
        dgoodsBuyMapper.insertBatch(list);
        appendScopeCache(termKey, DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, termMap, valueSetMap);
    }

    @Override
    public List<DgoodsBuyVo> listUserScope(String channelCode) {
        return dgoodsBuyMapper.selectVoList(Wrappers.lambdaQuery(DgoodsBuy.class)
            .eq(DgoodsBuy::getChannelCode, channelCode)
            .orderByAsc(DgoodsBuy::getDgoodsBuyType)
            .orderByAsc(DgoodsBuy::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserScopeByIds(Collection<Long> ids) {
        String tenantId = LoginHelper.getTenantId();
        List<DgoodsBuy> list = dgoodsBuyMapper.selectList(Wrappers.lambdaQuery(DgoodsBuy.class).in(DgoodsBuy::getId, ids));
        for (DgoodsBuy buy : list) {
            removeScopeValueCache(
                String.format(DisRedisConstants.KEY_USER_SCOPE_TERM, tenantId, buy.getChannelCode()),
                String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, buy.getChannelCode(), buy.getDgoodsBuyType()),
                buy.getDgoodsBuyType(),
                buy.getDgoodsBuyValue()
            );
        }
        dgoodsBuyMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdatePublishConfig(DisGoodsPublishConfigSaveDTO dto) {
        String tenantId = LoginHelper.getTenantId();
        String channelCode = dto.getChannelCode();
        Dgoods entity = dgoodsMapper.selectOne(Wrappers.lambdaQuery(Dgoods.class)
            .eq(Dgoods::getTenantId, tenantId)
            .eq(Dgoods::getChannelCode, channelCode));
        if (entity == null) {
            entity = new Dgoods();
            entity.setTenantId(tenantId);
            entity.setChannelCode(channelCode);
            copyPublishFields(dto, entity);
            dgoodsMapper.insert(entity);
        } else {
            copyPublishFields(dto, entity);
            dgoodsMapper.updateById(entity);
        }
        putPublishConfigCache(tenantId, channelCode, entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultPublishConfig(String tenantId, String channelCode) {
        Long count = dgoodsMapper.selectCount(Wrappers.lambdaQuery(Dgoods.class)
            .eq(Dgoods::getTenantId, tenantId)
            .eq(Dgoods::getChannelCode, channelCode));
        if (count != null && count > 0) {
            return;
        }
        Dgoods entity = new Dgoods();
        entity.setTenantId(tenantId);
        entity.setChannelCode(channelCode);
        entity.setDgoodsType(1L);
        entity.setDgoodsSync(1L);
        entity.setDgoodsPut(1L);
        entity.setDgoodsUprice(1L);
        entity.setDgoodsUnum(1L);
        entity.setDgoodsEdit(1L);
        entity.setProductNonAll(0L);
        dgoodsMapper.insert(entity);
        putPublishConfigCache(tenantId, channelCode, entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByChannel(String tenantId, String channelCode) {
        dgoodsMapper.delete(Wrappers.lambdaQuery(Dgoods.class)
            .eq(Dgoods::getTenantId, tenantId)
            .eq(Dgoods::getChannelCode, channelCode));
        redisTemplate.delete(String.format(DisRedisConstants.KEY_PUBLISH_CONFIG, tenantId, channelCode));
        dgoodsBuyMapper.delete(Wrappers.lambdaQuery(DgoodsBuy.class)
            .eq(DgoodsBuy::getTenantId, tenantId)
            .eq(DgoodsBuy::getChannelCode, channelCode));
        redisTemplate.delete(Arrays.asList(
            String.format(DisRedisConstants.KEY_USER_SCOPE_TERM, tenantId, channelCode),
            String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, "USER"),
            String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, "ORG"),
            String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, "PROVINCE"),
            String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, "CITY"),
            String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, "DISTRICT")
        ));
        dgoodsScopelistMapper.delete(Wrappers.lambdaQuery(DgoodsScopelist.class)
            .eq(DgoodsScopelist::getTenantId, tenantId)
            .eq(DgoodsScopelist::getChannelCode, channelCode));
        redisTemplate.delete(Arrays.asList(
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_TERM, tenantId, channelCode),
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, "SKU"),
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, "CATEGORY"),
            String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, "BRAND")
        ));
        List<PriceRule> priceRules = priceRuleMapper.selectList(Wrappers.lambdaQuery(PriceRule.class)
            .eq(PriceRule::getTenantId, tenantId)
            .eq(PriceRule::getChannelCode, channelCode));
        List<String> priceRedisKeys = new ArrayList<>();
        for (PriceRule rule : priceRules) {
            priceRuleScopeMapper.physicalDeleteByRuleId(tenantId, rule.getId());
            String ruleIdStr = String.valueOf(rule.getId());
            priceRedisKeys.add(String.format(DisRedisConstants.KEY_PRICE_RULE_DETAIL, tenantId, ruleIdStr));
            priceRedisKeys.add(String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "1"));
            priceRedisKeys.add(String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "2"));
            priceRedisKeys.add(String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "3"));
            priceRedisKeys.add(String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "4"));
        }
        priceRuleMapper.delete(Wrappers.lambdaQuery(PriceRule.class)
            .eq(PriceRule::getTenantId, tenantId)
            .eq(PriceRule::getChannelCode, channelCode));
        priceRedisKeys.add(String.format(DisRedisConstants.KEY_PRICE_RULE_ZSET, tenantId, channelCode));
        redisTemplate.delete(priceRedisKeys);
        List<StockRule> stockRules = stockRuleMapper.selectList(Wrappers.lambdaQuery(StockRule.class)
            .eq(StockRule::getTenantId, tenantId)
            .eq(StockRule::getChannelCode, channelCode));
        List<String> stockRedisKeys = new ArrayList<>();
        for (StockRule rule : stockRules) {
            stockRedisKeys.add(String.format(DisRedisConstants.KEY_STOCK_RULE_DETAIL, tenantId, rule.getId()));
        }
        stockRuleMapper.delete(Wrappers.lambdaQuery(StockRule.class)
            .eq(StockRule::getTenantId, tenantId)
            .eq(StockRule::getChannelCode, channelCode));
        stockRedisKeys.add(String.format(DisRedisConstants.KEY_STOCK_RULE_ZSET, tenantId, channelCode));
        redisTemplate.delete(stockRedisKeys);
        List<Long> channelSkuIds = remoteGoodsService.removeGoodsByChannel(tenantId, channelCode);
        if (CollUtil.isNotEmpty(channelSkuIds)) {
            for (Long skuId : channelSkuIds) {
                try {
                    String docId = channelCode + "_" + skuId;
                    restHighLevelClient.delete(new DeleteRequest(DisGoodsMqConstants.ES_INDEX_DIS_GOODS, docId),
                        RequestOptions.DEFAULT);
                } catch (Exception e) {
                    log.error("删除渠道商品 ES 失败, channel={}, skuId={}", channelCode, skuId, e);
                }
            }
        }
    }

    @Override
    public DgoodsVo getPublishConfig(String channelCode) {
        return dgoodsMapper.selectVoOne(Wrappers.lambdaQuery(Dgoods.class)
            .eq(Dgoods::getChannelCode, channelCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveGoodsScope(DisGoodsScopeSaveDTO dto) {
        if (CollUtil.isEmpty(dto.getScopeItems())) {
            return;
        }
        String tenantId = LoginHelper.getTenantId();
        String channelCode = dto.getChannelCode();
        String termKey = String.format(DisRedisConstants.KEY_GOODS_SCOPE_TERM, tenantId, channelCode);
        List<DgoodsScopelist> list = new ArrayList<>(dto.getScopeItems().size());
        Map<String, String> termMap = new HashMap<>();
        Map<String, List<String>> valueSetMap = new HashMap<>();
        for (DisGoodsScopeItem item : dto.getScopeItems()) {
            putScopeTerm(termMap, termKey, item.getScopeType(), item.getScopeTerm());
            DgoodsScopelist entity = new DgoodsScopelist();
            entity.setTenantId(tenantId);
            entity.setChannelCode(channelCode);
            entity.setScopeType(item.getScopeType());
            entity.setScopeTerm(item.getScopeTerm());
            entity.setScopeValue(item.getScopeValue());
            entity.setRuleValueName(item.getRuleValueName());
            list.add(entity);
            valueSetMap.computeIfAbsent(item.getScopeType(), k -> new ArrayList<>()).add(item.getScopeValue());
        }
        dgoodsScopelistMapper.insertBatch(list);
        appendScopeCache(termKey, DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, channelCode, termMap, valueSetMap);
    }

    @Override
    public List<DgoodsScopelistVo> listGoodsScope(String channelCode) {
        return dgoodsScopelistMapper.selectVoList(Wrappers.lambdaQuery(DgoodsScopelist.class)
            .eq(DgoodsScopelist::getChannelCode, channelCode)
            .orderByAsc(DgoodsScopelist::getScopeType)
            .orderByAsc(DgoodsScopelist::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoodsScopeByIds(Collection<Long> ids) {
        String tenantId = LoginHelper.getTenantId();
        List<DgoodsScopelist> list = dgoodsScopelistMapper.selectList(
            Wrappers.lambdaQuery(DgoodsScopelist.class).in(DgoodsScopelist::getId, ids));
        for (DgoodsScopelist scope : list) {
            removeScopeValueCache(
                String.format(DisRedisConstants.KEY_GOODS_SCOPE_TERM, tenantId, scope.getChannelCode()),
                String.format(DisRedisConstants.KEY_GOODS_SCOPE_SET, tenantId, scope.getChannelCode(), scope.getScopeType()),
                scope.getScopeType(),
                scope.getScopeValue()
            );
        }
        dgoodsScopelistMapper.deleteByIds(ids);
    }

    private void copyPublishFields(DisGoodsPublishConfigSaveDTO dto, Dgoods entity) {
        entity.setDgoodsType(dto.getDgoodsType());
        entity.setDgoodsSync(dto.getDgoodsSync());
        entity.setDgoodsPut(dto.getDgoodsPut());
        entity.setDgoodsUprice(dto.getDgoodsUprice());
        entity.setDgoodsUnum(dto.getDgoodsUnum());
        entity.setDgoodsEdit(dto.getDgoodsEdit());
        entity.setProductNonAll(dto.getProductNonAll());
    }

    private void putPublishConfigCache(String tenantId, String channelCode, Dgoods entity) {
        String redisKey = String.format(DisRedisConstants.KEY_PUBLISH_CONFIG, tenantId, channelCode);
        Map<String, String> map = new HashMap<>(8);
        map.put("dgoodsType", String.valueOf(entity.getDgoodsType()));
        map.put("dgoodsSync", String.valueOf(entity.getDgoodsSync()));
        map.put("dgoodsPut", String.valueOf(entity.getDgoodsPut()));
        map.put("dgoodsUprice", String.valueOf(entity.getDgoodsUprice()));
        map.put("dgoodsUnum", String.valueOf(entity.getDgoodsUnum()));
        map.put("dgoodsEdit", String.valueOf(entity.getDgoodsEdit()));
        map.put("productNonAll", String.valueOf(entity.getProductNonAll()));
        redisTemplate.opsForHash().putAll(redisKey, map);
    }


    private void putScopeTerm(Map<String, String> termMap, String termKey, String type, String term) {
        String batchTerm = termMap.get(type);
        if (batchTerm != null && !batchTerm.equalsIgnoreCase(term)) {
            throw new ServiceException("同一维度运算符必须一致: " + type);
        }
        Object existTerm = redisTemplate.opsForHash().get(termKey, type);
        if (existTerm != null && !existTerm.toString().equalsIgnoreCase(term)) {
            throw new ServiceException("维度 " + type + " 已存在运算符 " + existTerm + "，请先删除后再改");
        }
        termMap.put(type, term);
    }

    private void appendScopeCache(String termKey, String setKeyPattern,
                                  String tenantId, String channelCode,
                                  Map<String, String> termMap, Map<String, List<String>> valueSetMap) {
        if (CollUtil.isNotEmpty(termMap)) {
            redisTemplate.opsForHash().putAll(termKey, termMap);
        }
        if (CollUtil.isEmpty(valueSetMap)) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : valueSetMap.entrySet()) {
            redisTemplate.opsForSet().add(
                String.format(setKeyPattern, tenantId, channelCode, entry.getKey()),
                entry.getValue().toArray(new String[0])
            );
        }
    }

    private void removeScopeValueCache(String termKey, String setKey, String type, String value) {
        redisTemplate.opsForSet().remove(setKey, value);
        Long size = redisTemplate.opsForSet().size(setKey);
        if (size == null || size == 0L) {
            redisTemplate.delete(setKey);
            redisTemplate.opsForHash().delete(termKey, type);
        }
    }
}
