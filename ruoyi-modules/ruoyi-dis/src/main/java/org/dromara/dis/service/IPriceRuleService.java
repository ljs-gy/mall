package org.dromara.dis.service;

import org.dromara.dis.domain.dto.DisPriceRuleSaveDTO;
import org.dromara.dis.domain.vo.PriceRuleVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;


public interface IPriceRuleService {

    PriceRuleVo queryById(Long id);

    TableDataInfo<PriceRuleVo> queryPageList(String channelCode, String ruleName, PageQuery pageQuery);

    List<PriceRuleVo> queryList(String channelCode);

    Long saveOrUpdatePriceRule(DisPriceRuleSaveDTO dto);

    Boolean deleteByIds(Collection<Long> ids);
}
