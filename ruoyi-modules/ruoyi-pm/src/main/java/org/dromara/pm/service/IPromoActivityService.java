package org.dromara.pm.service;

import org.dromara.pm.domain.vo.PromoActivityVo;
import org.dromara.pm.domain.bo.PromoActivityAuditBo;
import org.dromara.pm.domain.bo.PromoActivityBo;
import org.dromara.pm.domain.bo.ExclusivePromoActivityBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;


public interface IPromoActivityService {

    PromoActivityVo queryById(Long id);

    TableDataInfo<PromoActivityVo> queryPageList(PromoActivityBo bo, PageQuery pageQuery);

    List<PromoActivityVo> queryList(PromoActivityBo bo);


    Boolean insertByBo(PromoActivityBo bo);


    Long createSeckillActivity(ExclusivePromoActivityBo bo);

    /**
     * 新增特价活动
     */
    Long createSpecialPriceActivity(ExclusivePromoActivityBo bo);

    Boolean updateByBo(PromoActivityBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);


    void auditPromoActivity(PromoActivityAuditBo bo);


    void cachePromoActivity(Long activityId);


    void clearPromoActivityCache(Long activityId);


    void warmUpSeckillCache(Long activityId);


    void clearSeckillCache(Long activityId);


    void cacheSpecialPricePromo(Long activityId);


    void clearSpecialPriceCache(Long activityId);


    void cacheCrossFullReducePromo(Long activityId);


    void clearCrossFullReduceCache(Long activityId);


    void cacheCrossFullDiscountPromo(Long activityId);

    void clearCrossFullDiscountCache(Long activityId);

}
