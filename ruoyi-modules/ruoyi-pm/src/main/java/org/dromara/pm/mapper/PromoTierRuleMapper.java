package org.dromara.pm.mapper;

import org.dromara.pm.domain.PromoTierRule;
import org.dromara.pm.domain.vo.PromoTierRuleVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营销活动阶梯规则Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface PromoTierRuleMapper extends BaseMapperPlus<PromoTierRule, PromoTierRuleVo> {

    int batchInsert(@Param("list") List<PromoTierRule> list);

    List<PromoTierRule> selectListByActivityId(@Param("activityId") Long activityId);

}
