package org.dromara.pm.mapper;

import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.domain.vo.PromoGoodsSkuVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 活动商品配置Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface PromoGoodsSkuMapper extends BaseMapperPlus<PromoGoodsSku, PromoGoodsSkuVo> {

    int batchInsert(@Param("list") List<PromoGoodsSku> list);

    List<PromoGoodsSku> selectListByActivityId(@Param("activityId") Long activityId);


    List<Long> selectConflictedSkuIds(@Param("tenantId") String tenantId,
                                      @Param("skuIds") List<Long> skuIds,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime,
                                      @Param("excludeActivityId") Long excludeActivityId);

    /**
     * 支付成功：记已售（在线预占走 Redis）
     */
    int confirmPromoStock(@Param("tenantId") String tenantId,
                          @Param("activityId") Long activityId,
                          @Param("skuId") Long skuId,
                          @Param("qty") int qty);

    /**
     * 售后退款：已售配额回退（支付后逆向）
     */
    int rollbackSoldPromoStock(@Param("tenantId") String tenantId,
                               @Param("activityId") Long activityId,
                               @Param("skuId") Long skuId,
                               @Param("qty") int qty);
}
