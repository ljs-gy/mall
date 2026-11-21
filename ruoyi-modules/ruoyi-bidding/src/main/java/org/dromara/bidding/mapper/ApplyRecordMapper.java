package org.dromara.bidding.mapper;

import org.dromara.bidding.domain.ApplyRecord;
import org.dromara.bidding.domain.vo.ApplyRecordVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 报名记录Mapper接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface ApplyRecordMapper extends BaseMapperPlus<ApplyRecord, ApplyRecordVo> {

    /**
     * 排他锁查询报名单
     */
    ApplyRecord selectForUpdate(Long applyId);

    /**
     * 统计用户在场次下待审或已通过的报名单数量
     */
    int countActiveApply(Long userId, Long sessionId, String tenantId);

}
