package org.dromara.bidding.mapper;

import org.dromara.bidding.domain.AnnouncementItem;
import org.dromara.bidding.domain.vo.AnnouncementItemVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.math.BigDecimal;

/**
 * 竞价标的Mapper接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface AnnouncementItemMapper extends BaseMapperPlus<AnnouncementItem, AnnouncementItemVo> {

    /**
     * 更新标的最高出价进度（Lua 校验通过后同步 MySQL）
     */
    int updateMaxBidProgress(Long itemId, BigDecimal bidPrice, BigDecimal bidPriceUnit,
                             BigDecimal bidTotalMoney, Long userId, java.util.Date itemEndTime);

}
