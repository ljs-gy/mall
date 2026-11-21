package org.dromara.pm.mapper;

import org.dromara.pm.domain.PromoScopeTarget;
import org.dromara.pm.domain.vo.PromoScopeTargetVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营销范围Mapper接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface PromoScopeTargetMapper extends BaseMapperPlus<PromoScopeTarget, PromoScopeTargetVo> {

    int batchInsert(@Param("list") List<PromoScopeTarget> list);

    List<PromoScopeTarget> selectBySourceId(@Param("sourceId") Long sourceId,
                                            @Param("sourceType") Long sourceType);

}
