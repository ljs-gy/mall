package org.dromara.bidding.mapper;

import org.dromara.bidding.domain.ApplyItemRel;
import org.dromara.bidding.domain.vo.ApplyItemRelVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 报名标的关联Mapper接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface ApplyItemRelMapper extends BaseMapperPlus<ApplyItemRel, ApplyItemRelVo> {

    /**
     * 统计勾选商品中与已有待审/已通过申请重合的数量
     */
    int countDuplicateItems(Long userId, Long sessionId, String tenantId, List<Long> itemIds);

    /**
     * 查询报名单关联的标的ID列表
     */
    List<Long> selectItemIdsByApplyId(Long applyId);

}
