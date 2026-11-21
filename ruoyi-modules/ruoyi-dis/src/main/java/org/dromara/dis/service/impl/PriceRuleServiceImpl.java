package org.dromara.dis.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dis.api.constant.DisRedisConstants;
import org.dromara.dis.domain.PriceRule;
import org.dromara.dis.domain.PriceRuleScope;
import org.dromara.dis.domain.dto.DisPriceRuleSaveDTO;
import org.dromara.dis.domain.dto.DisPriceRuleScopeItem;
import org.dromara.dis.domain.vo.PriceRuleVo;
import org.dromara.dis.mapper.PriceRuleMapper;
import org.dromara.dis.mapper.PriceRuleScopeMapper;
import org.dromara.dis.service.IPriceRuleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PriceRuleServiceImpl implements IPriceRuleService {

    private final PriceRuleMapper priceRuleMapper;
    private final PriceRuleScopeMapper priceRuleScopeMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public PriceRuleVo queryById(Long id) {
        PriceRuleVo vo = priceRuleMapper.selectVoById(id);
        if (vo != null) {
            vo.setScopeItems(priceRuleScopeMapper.selectVoList(
                Wrappers.lambdaQuery(PriceRuleScope.class)
                    .eq(PriceRuleScope::getPriceRuleId, id)
                    .orderByAsc(PriceRuleScope::getScopeType)
                    .orderByAsc(PriceRuleScope::getId)));
        }
        return vo;
    }

    @Override
    public TableDataInfo<PriceRuleVo> queryPageList(String channelCode, String ruleName, PageQuery pageQuery) {
        Page<PriceRuleVo> page = priceRuleMapper.selectVoPage(pageQuery.build(), Wrappers.lambdaQuery(PriceRule.class)
            .eq(StringUtils.isNotBlank(channelCode), PriceRule::getChannelCode, channelCode)
            .like(StringUtils.isNotBlank(ruleName), PriceRule::getRuleName, ruleName)
            .orderByAsc(PriceRule::getDpriceWeight)
            .orderByDesc(PriceRule::getId));
        return TableDataInfo.build(page);
    }

    @Override
    public List<PriceRuleVo> queryList(String channelCode) {
        return priceRuleMapper.selectVoList(Wrappers.lambdaQuery(PriceRule.class)
            .eq(StringUtils.isNotBlank(channelCode), PriceRule::getChannelCode, channelCode)
            .orderByAsc(PriceRule::getDpriceWeight));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdatePriceRule(DisPriceRuleSaveDTO dto) {
        String tenantId = LoginHelper.getTenantId();
        PriceRule rule = new PriceRule();
        rule.setTenantId(tenantId);
        rule.setChannelCode(dto.getChannelCode());
        rule.setRuleName(dto.getRuleName());
        rule.setDpriceSyncType(dto.getDpriceSyncType());
        rule.setSalesPriceRuleVal(dto.getSalesPriceRuleVal());
        rule.setMarketPriceRuleVal(dto.getMarketPriceRuleVal());
        rule.setDefaultSalesPrice(dto.getDefaultSalesPrice());
        rule.setDefaultMarketPrice(dto.getDefaultMarketPrice());
        rule.setDpriceSync(dto.getDpriceSync());
        rule.setDpriceVer(dto.getDpriceVer());
        rule.setDpriceWeight(dto.getDpriceWeight());
        rule.setDpriceSydate(dto.getDpriceSydate());
        rule.setDpriceEydate(dto.getDpriceEydate());
        if (dto.getId() == null) {
            priceRuleMapper.insert(rule);
        } else {
            rule.setId(dto.getId());
            if (priceRuleMapper.updateById(rule) <= 0) {
                throw new ServiceException("价格规则不存在");
            }
        }
        Long ruleId = rule.getId();
        priceRuleScopeMapper.physicalDeleteByRuleId(tenantId, ruleId);
        Map<Integer, List<String>> scopeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(dto.getScopeItems())) {
            List<PriceRuleScope> scopes = new ArrayList<>(dto.getScopeItems().size());
            for (DisPriceRuleScopeItem item : dto.getScopeItems()) {
                PriceRuleScope scope = new PriceRuleScope();
                scope.setTenantId(tenantId);
                scope.setPriceRuleId(ruleId);
                scope.setScopeType(item.getScopeType());
                scope.setScopeValue(item.getScopeValue());
                scopes.add(scope);
                scopeMap.computeIfAbsent(item.getScopeType().intValue(), k -> new ArrayList<>()).add(item.getScopeValue());
            }
            priceRuleScopeMapper.insertBatch(scopes);
        }
        putPriceRuleCache(rule, scopeMap);
        return ruleId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> ids) {
        String tenantId = LoginHelper.getTenantId();
        List<PriceRule> rules = priceRuleMapper.selectList(
            Wrappers.lambdaQuery(PriceRule.class).in(PriceRule::getId, ids));
        for (PriceRule rule : rules) {
            priceRuleScopeMapper.physicalDeleteByRuleId(tenantId, rule.getId());
            removePriceRuleCache(tenantId, rule.getChannelCode(), rule.getId());
        }
        return priceRuleMapper.deleteByIds(ids) > 0;
    }

    private void putPriceRuleCache(PriceRule rule, Map<Integer, List<String>> scopeMap) {
        String tenantId = rule.getTenantId();
        String channelCode = rule.getChannelCode();
        String ruleIdStr = String.valueOf(rule.getId());
        redisTemplate.opsForZSet().add(
            String.format(DisRedisConstants.KEY_PRICE_RULE_ZSET, tenantId, channelCode),
            ruleIdStr,
            rule.getDpriceWeight().doubleValue());
        redisTemplate.opsForValue().set(
            String.format(DisRedisConstants.KEY_PRICE_RULE_DETAIL, tenantId, ruleIdStr),
            JSON.toJSONString(rule));

        redisTemplate.delete(Arrays.asList(
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "1"),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "2"),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "3"),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "4")
        ));
        if (CollUtil.isEmpty(scopeMap)) {
            return;
        }
        for (Map.Entry<Integer, List<String>> entry : scopeMap.entrySet()) {
            redisTemplate.opsForSet().add(
                String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, entry.getKey()),
                entry.getValue().toArray(new String[0]));
        }
    }

    private void removePriceRuleCache(String tenantId, String channelCode, Long ruleId) {
        String ruleIdStr = String.valueOf(ruleId);
        redisTemplate.opsForZSet().remove(
            String.format(DisRedisConstants.KEY_PRICE_RULE_ZSET, tenantId, channelCode), ruleIdStr);
        redisTemplate.delete(Arrays.asList(
            String.format(DisRedisConstants.KEY_PRICE_RULE_DETAIL, tenantId, ruleIdStr),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "1"),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "2"),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "3"),
            String.format(DisRedisConstants.KEY_PRICE_RULE_SCOPE, tenantId, ruleIdStr, "4")
        ));
    }
}
