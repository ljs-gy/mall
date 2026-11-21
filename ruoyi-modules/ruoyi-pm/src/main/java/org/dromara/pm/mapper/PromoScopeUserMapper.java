package org.dromara.pm.mapper;

import org.dromara.pm.domain.PromoScopeUser;
import org.dromara.pm.domain.vo.PromoScopeUserVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营销活动与优惠券人群关系Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface PromoScopeUserMapper extends BaseMapperPlus<PromoScopeUser, PromoScopeUserVo> {

    int batchInsert(@Param("list") List<PromoScopeUser> list);

    List<PromoScopeUser> selectListBySourceId(@Param("sourceId") Long sourceId,
                                              @Param("sourceType") Long sourceType);

    List<Long> selectUserIdsBySourceId(@Param("sourceId") Long sourceId);

}
