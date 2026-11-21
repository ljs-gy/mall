package org.dromara.user.mapper;

import org.apache.ibatis.annotations.Param;
import org.dromara.user.domain.Userinfo;
import org.dromara.user.domain.vo.UserinfoVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 用户信息Mapper接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface UserinfoMapper extends BaseMapperPlus<Userinfo, UserinfoVo> {

    /**
     * 游标分页：指定渠道下全部有效采购商 userinfoId
     */
    List<Long> selectBuyerUserinfoIdsAfter(@Param("channelCode") String channelCode,
                                           @Param("lastId") long lastId,
                                           @Param("size") int size);

    /**
     * 按渠道 + 会员/分销等级一次查出有效采购商 userinfoId
     */
    List<Long> selectBuyerUserinfoIdsByDistLevels(@Param("channelCode") String channelCode,
                                                  @Param("distLevelIds") List<Long> distLevelIds);

}
