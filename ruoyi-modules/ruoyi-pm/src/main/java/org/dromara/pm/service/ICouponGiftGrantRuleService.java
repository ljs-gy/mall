package org.dromara.pm.service;

import org.dromara.pm.domain.vo.CouponGiftGrantRuleVo;
import org.dromara.pm.domain.bo.CouponGiftGrantRuleAuditBo;
import org.dromara.pm.domain.bo.CouponGiftGrantRuleBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 满赠券发放规则Service接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface ICouponGiftGrantRuleService {

    CouponGiftGrantRuleVo queryById(Long id);

    TableDataInfo<CouponGiftGrantRuleVo> queryPageList(CouponGiftGrantRuleBo bo, PageQuery pageQuery);

    List<CouponGiftGrantRuleVo> queryList(CouponGiftGrantRuleBo bo);


    Boolean insertByBo(CouponGiftGrantRuleBo bo);

    Boolean updateByBo(CouponGiftGrantRuleBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);


    void auditGiftGrantRule(CouponGiftGrantRuleAuditBo bo);


    void cacheGiftGrantRule(Long ruleId);


    void clearGiftGrantRuleCache(Long ruleId);

}
