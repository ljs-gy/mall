package org.dromara.pm.mapper;

import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.vo.CouponTemplateVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface CouponTemplateMapper extends BaseMapperPlus<CouponTemplate, CouponTemplateVo> {

    /**
     * CAS 更新状态
     */
    int updateStatusWithCas(@Param("id") Long id,
                            @Param("newStatus") Long newStatus,
                            @Param("oldStatus") Long oldStatus);

    /**
     * 未开始/进行中 -> 已结束
     */
    int updateStatusToEnded(@Param("id") Long id);

}
