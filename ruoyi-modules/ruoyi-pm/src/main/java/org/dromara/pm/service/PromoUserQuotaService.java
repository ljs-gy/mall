package org.dromara.pm.service;

import org.dromara.pm.domain.PromoActivity;

import java.util.List;


public interface PromoUserQuotaService {


    boolean hasRemainingJoinQuota(String tenant, String channel, Long userId, PromoActivity activity);


    List<Long> reserveJoinQuota(String tenant, String channel, Long userId, List<Long> activityIds,
                                String parentOrderNo);

    void rollbackJoinQuota(String tenant, String channel, Long userId, List<Long> activityIds);
}
