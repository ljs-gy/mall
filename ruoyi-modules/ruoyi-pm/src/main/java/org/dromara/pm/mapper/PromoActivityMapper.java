package org.dromara.pm.mapper;

import org.dromara.pm.domain.PromoActivity;
import org.dromara.pm.domain.vo.PromoActivityVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

/**
 * 营销活动主Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface PromoActivityMapper extends BaseMapperPlus<PromoActivity, PromoActivityVo> {

    int updateStatusWithCas(@Param("id") Long id,
                            @Param("newStatus") Long newStatus,
                            @Param("oldStatus") Long oldStatus);

    int updateStatusToEnded(@Param("id") Long id);

}
