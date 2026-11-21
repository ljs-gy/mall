package org.dromara.dis.service;

import org.dromara.dis.domain.bo.BuyerContextBo;

import java.util.List;

/**
 * 买家渠道准入
 *
 * @author ljs
 * @date 2026-08-08
 */
public interface IBuyerAccessService {

    /**
     * 提取登录用户上下文（含区域 / 组织编码）
     */
    BuyerContextBo getBuyerContext(Long userinfoId);

    /**
     * 计算买家允许访问的渠道编码列表；未命中时回退 defaultChannelCode
     */
    List<String> getAccessibleChannels(String tenantId, String defaultChannelCode, BuyerContextBo buyer);

    /**
     * 校验单个渠道的用户范围规则
     */
    boolean checkChannelAccess(String tenantId, String channelCode, BuyerContextBo buyer);
}
