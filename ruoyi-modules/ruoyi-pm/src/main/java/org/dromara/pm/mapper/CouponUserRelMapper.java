package org.dromara.pm.mapper;

import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.domain.vo.CouponUserRelVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 用户优惠券领用记录Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface CouponUserRelMapper extends BaseMapperPlus<CouponUserRel, CouponUserRelVo> {

    int batchInsert(@Param("list") List<CouponUserRel> list);

    /**
     * 本模板下已持有券的用户
     */
    List<Long> selectExistingUserIds(@Param("templateId") Long templateId, @Param("userIds") List<Long> userIds);

    /**
     * 查询用户未使用且在有效期内的优惠券
     */
    List<CouponUserRel> selectUnusedCoupons(@Param("tenantId") String tenantId,
                                            @Param("userId") Long userId,
                                            @Param("now") Date now);

    /**
     * 下单核销：0未使用 → 1已使用
     */
    int useCoupon(@Param("tenantId") String tenantId,
                  @Param("userCouponId") Long userCouponId,
                  @Param("userId") Long userId);

    /**
     * 关单/退券回滚：1已使用 → 0未使用 / 2已过期
     */
    int rollbackCoupon(@Param("tenantId") String tenantId,
                       @Param("userCouponId") Long userCouponId,
                       @Param("userId") Long userId,
                       @Param("now") Date now);
}
