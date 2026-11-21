package org.dromara.pm.mapper;

import org.dromara.pm.domain.PromoTierGift;
import org.dromara.pm.domain.vo.PromoTierGiftVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营销活动赠品明细Mapper
 */
public interface PromoTierGiftMapper extends BaseMapperPlus<PromoTierGift, PromoTierGiftVo> {

    int batchInsert(@Param("list") List<PromoTierGift> list);

    List<PromoTierGift> selectListByActivityId(@Param("activityId") Long activityId);

}
