package org.dromara.dis.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.dis.domain.dto.DisStockRuleSaveDTO;
import org.dromara.dis.domain.vo.StockRuleVo;

import java.util.Collection;
import java.util.List;

public interface IStockRuleService {

    StockRuleVo queryById(Long id);

    TableDataInfo<StockRuleVo> queryPageList(String channelCode, String ruleName, PageQuery pageQuery);

    List<StockRuleVo> queryList(String channelCode);

    Long saveOrUpdateStockRule(DisStockRuleSaveDTO dto);

    Boolean deleteByIds(Collection<Long> ids);
}
