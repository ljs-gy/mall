package org.dromara.pm.mapper;

import org.dromara.pm.domain.CouponGiftGrantRule;
import org.dromara.pm.domain.vo.CouponGiftGrantRuleVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

/**
 * 满赠券发放规则Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface CouponGiftGrantRuleMapper extends BaseMapperPlus<CouponGiftGrantRule, CouponGiftGrantRuleVo> {

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
