package org.dromara.pm.service;

import org.dromara.pm.domain.PromoMutexGroup;
import org.dromara.pm.domain.vo.PromoMutexGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 营销互斥组配置
 */
public interface IPromoMutexGroupService {

    /**
     * 查询启用中的互斥组配置（按 priority 降序）
     */
    List<PromoMutexGroupVo> listEnabled();

    /**
     * promoType -> 配置
     */
    Map<String, PromoMutexGroup> mapEnabledByPromoType();

    /**
     * 按活动类型查询互斥配置
     */
    PromoMutexGroup getEnabledByPromoType(String promoType);

}
