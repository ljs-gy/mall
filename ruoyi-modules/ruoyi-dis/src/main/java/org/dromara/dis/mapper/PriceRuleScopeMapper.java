package org.dromara.dis.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.dis.domain.PriceRuleScope;
import org.dromara.dis.domain.vo.PriceRuleScopeVo;

/**
 * 价格规则适用范围Mapper接口
 */
public interface PriceRuleScopeMapper extends BaseMapperPlus<PriceRuleScope, PriceRuleScopeVo> {

    @InterceptorIgnore(tenantLine = "true")
    @Delete("DELETE FROM dis_price_rule_scope WHERE tenant_id = #{tenantId} AND price_rule_id = #{priceRuleId}")
    int physicalDeleteByRuleId(@Param("tenantId") String tenantId, @Param("priceRuleId") Long priceRuleId);
}
