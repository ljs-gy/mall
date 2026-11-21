package org.dromara.dis.service.impl;

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
import org.dromara.dis.domain.StockRule;
import org.dromara.dis.domain.dto.DisStockRuleSaveDTO;
import org.dromara.dis.domain.vo.StockRuleVo;
import org.dromara.dis.mapper.StockRuleMapper;
import org.dromara.dis.service.IStockRuleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockRuleServiceImpl implements IStockRuleService {

    private final StockRuleMapper stockRuleMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public StockRuleVo queryById(Long id) {
        return stockRuleMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<StockRuleVo> queryPageList(String channelCode, String ruleName, PageQuery pageQuery) {
        Page<StockRuleVo> page = stockRuleMapper.selectVoPage(pageQuery.build(), Wrappers.lambdaQuery(StockRule.class)
            .eq(StringUtils.isNotBlank(channelCode), StockRule::getChannelCode, channelCode)
            .like(StringUtils.isNotBlank(ruleName), StockRule::getRuleName, ruleName)
            .orderByAsc(StockRule::getDgnumWeight)
            .orderByDesc(StockRule::getId));
        return TableDataInfo.build(page);
    }

    @Override
    public List<StockRuleVo> queryList(String channelCode) {
        return stockRuleMapper.selectVoList(Wrappers.lambdaQuery(StockRule.class)
            .eq(StringUtils.isNotBlank(channelCode), StockRule::getChannelCode, channelCode)
            .orderByAsc(StockRule::getDgnumWeight));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateStockRule(DisStockRuleSaveDTO dto) {
        String tenantId = LoginHelper.getTenantId();
        StockRule rule = new StockRule();
        rule.setTenantId(tenantId);
        rule.setChannelCode(dto.getChannelCode());
        rule.setRuleName(dto.getRuleName());
        rule.setDgnumPro(dto.getDgnumPro());
        rule.setDgnumPrice(dto.getDgnumPrice());
        rule.setDgnumSync(dto.getDgnumSync());
        rule.setDgnumMin(dto.getDgnumMin());
        rule.setDgnumVer(dto.getDgnumVer());
        rule.setDgnumWeight(dto.getDgnumWeight());
        rule.setDgnumStydate(dto.getDgnumStydate());
        rule.setDgnumEydate(dto.getDgnumEydate());
        if (dto.getId() == null) {
            stockRuleMapper.insert(rule);
        } else {
            rule.setId(dto.getId());
            if (stockRuleMapper.updateById(rule) <= 0) {
                throw new ServiceException("库存规则不存在");
            }
        }
        putStockRuleCache(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> ids) {
        String tenantId = LoginHelper.getTenantId();
        List<StockRule> rules = stockRuleMapper.selectList(
            Wrappers.lambdaQuery(StockRule.class).in(StockRule::getId, ids));
        for (StockRule rule : rules) {
            removeStockRuleCache(tenantId, rule.getChannelCode(), rule.getId());
        }
        return stockRuleMapper.deleteByIds(ids) > 0;
    }

    private void putStockRuleCache(StockRule rule) {
        String tenantId = rule.getTenantId();
        String ruleIdStr = String.valueOf(rule.getId());
        redisTemplate.opsForZSet().add(
            String.format(DisRedisConstants.KEY_STOCK_RULE_ZSET, tenantId, rule.getChannelCode()),
            ruleIdStr,
            rule.getDgnumWeight().doubleValue());
        redisTemplate.opsForValue().set(
            String.format(DisRedisConstants.KEY_STOCK_RULE_DETAIL, tenantId, ruleIdStr),
            JSON.toJSONString(rule));
    }

    private void removeStockRuleCache(String tenantId, String channelCode, Long ruleId) {
        String ruleIdStr = String.valueOf(ruleId);
        redisTemplate.opsForZSet().remove(
            String.format(DisRedisConstants.KEY_STOCK_RULE_ZSET, tenantId, channelCode), ruleIdStr);
        redisTemplate.delete(String.format(DisRedisConstants.KEY_STOCK_RULE_DETAIL, tenantId, ruleIdStr));
    }
}
